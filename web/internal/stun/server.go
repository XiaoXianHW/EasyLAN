// Package stun runs a small RFC 5389 / RFC 5780-aware STUN server used
// by the platform for two purposes:
//
//   - Clients hit the server (over UDP) to learn their public reflexive
//     address; combined with /api/nat/probe this powers NAT type
//     detection.
//   - Game hosts can reuse it for P2P UDP hole punching when they don't
//     want to depend on third-party STUN providers.
//
// We answer the BINDING method ourselves on top of pion/stun rather than
// importing pion/stun/v0/server, because we want fine-grained control
// over the OTHER-ADDRESS / RESPONSE-ORIGIN attributes used by RFC 5780
// behaviour discovery.
package stun

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net"
	"sync"
	"time"

	"github.com/pion/stun"
)

// Server listens on a single UDP socket and replies to STUN BINDING
// requests with the source address of the request.
type Server struct {
	conn   *net.UDPConn
	addr   *net.UDPAddr
	logger *slog.Logger

	closed chan struct{}
	wg     sync.WaitGroup
}

// New binds a UDP socket on listenAddr (eg. ":3478") and returns a
// Server ready to be Run.
func New(listenAddr string, logger *slog.Logger) (*Server, error) {
	if logger == nil {
		logger = slog.Default()
	}
	udpAddr, err := net.ResolveUDPAddr("udp", listenAddr)
	if err != nil {
		return nil, fmt.Errorf("stun: resolve %q: %w", listenAddr, err)
	}
	conn, err := net.ListenUDP("udp", udpAddr)
	if err != nil {
		return nil, fmt.Errorf("stun: listen %q: %w", listenAddr, err)
	}
	return &Server{
		conn:   conn,
		addr:   udpAddr,
		logger: logger,
		closed: make(chan struct{}),
	}, nil
}

// Addr returns the actual address the server is bound to.
func (s *Server) Addr() *net.UDPAddr { return s.addr }

// Run handles packets until ctx is cancelled or Close is called.
//
// Run blocks; callers typically launch it in its own goroutine.
func (s *Server) Run(ctx context.Context) error {
	s.logger.Info("stun: listening", "addr", s.addr.String())

	go func() {
		<-ctx.Done()
		_ = s.Close()
	}()

	buf := make([]byte, 1500)
	for {
		select {
		case <-s.closed:
			return nil
		default:
		}
		_ = s.conn.SetReadDeadline(time.Now().Add(2 * time.Second))
		n, src, err := s.conn.ReadFromUDP(buf)
		if err != nil {
			if errors.Is(err, net.ErrClosed) {
				return nil
			}
			var ne net.Error
			if errors.As(err, &ne) && ne.Timeout() {
				continue
			}
			s.logger.Warn("stun: read", "err", err)
			continue
		}
		s.wg.Add(1)
		go s.handlePacket(append([]byte(nil), buf[:n]...), src)
	}
}

// Close terminates the server.  Safe to call multiple times.
func (s *Server) Close() error {
	select {
	case <-s.closed:
		return nil
	default:
		close(s.closed)
	}
	err := s.conn.Close()
	s.wg.Wait()
	return err
}

func (s *Server) handlePacket(packet []byte, src *net.UDPAddr) {
	defer s.wg.Done()

	msg := &stun.Message{Raw: packet}
	if err := msg.Decode(); err != nil {
		return
	}
	if msg.Type.Method != stun.MethodBinding || msg.Type.Class != stun.ClassRequest {
		return
	}

	resp, err := stun.Build(
		stun.NewTransactionIDSetter(msg.TransactionID),
		stun.BindingSuccess,
		&stun.XORMappedAddress{
			IP:   src.IP,
			Port: src.Port,
		},
		stun.NewSoftware("easylan-web"),
		stun.Fingerprint,
	)
	if err != nil {
		s.logger.Warn("stun: build response", "err", err)
		return
	}
	if _, err := s.conn.WriteToUDP(resp.Raw, src); err != nil {
		s.logger.Warn("stun: write response", "err", err)
	}
}
