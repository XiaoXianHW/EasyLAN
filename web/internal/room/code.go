package room

import (
	"crypto/rand"
	"encoding/binary"
	"errors"
	"strings"
)

// codeAlphabet intentionally omits 0/O/I/L/1 to avoid visual ambiguity.
const codeAlphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

// CodeLength is the total number of characters in a room code,
// excluding the separating dash.
const CodeLength = 10

// CodeSeparator is the position of the dash inside the formatted code.
const CodeSeparator = 5

// GenerateCode returns a freshly-randomised XXXXX-XXXXX code.
func GenerateCode() (string, error) {
	var buf [CodeLength]byte
	for i := 0; i < CodeLength; i++ {
		c, err := randomCharacter(codeAlphabet)
		if err != nil {
			return "", err
		}
		buf[i] = c
	}
	return string(buf[:CodeSeparator]) + "-" + string(buf[CodeSeparator:]), nil
}

// NormalizeCode strips whitespace and uppercases input so callers can
// be lenient about how users type their codes (with or without dashes,
// mixed case, etc.).
func NormalizeCode(input string) string {
	cleaned := strings.Map(func(r rune) rune {
		switch {
		case r >= 'a' && r <= 'z':
			return r - 'a' + 'A'
		case r >= 'A' && r <= 'Z':
			return r
		case r >= '0' && r <= '9':
			return r
		default:
			return -1
		}
	}, input)
	if len(cleaned) == CodeLength {
		return cleaned[:CodeSeparator] + "-" + cleaned[CodeSeparator:]
	}
	return cleaned
}

// ValidateCode returns an error if code is not a valid XXXXX-XXXXX value.
func ValidateCode(code string) error {
	if len(code) != CodeLength+1 || code[CodeSeparator] != '-' {
		return errors.New("invalid code format, expected XXXXX-XXXXX")
	}
	for i, r := range code {
		if i == CodeSeparator {
			continue
		}
		if !strings.ContainsRune(codeAlphabet, r) {
			return errors.New("invalid character in code")
		}
	}
	return nil
}

func randomCharacter(alphabet string) (byte, error) {
	var b [8]byte
	if _, err := rand.Read(b[:]); err != nil {
		return 0, err
	}
	n := binary.BigEndian.Uint64(b[:])
	return alphabet[n%uint64(len(alphabet))], nil
}
