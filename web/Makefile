## EasyLAN web platform — Makefile
##
## Common targets for local development.

GO            ?= go
NPM           ?= npm
BIN           ?= easylan-web
FRONTEND_DIR  := frontend
EMBED_DIR     := internal/frontend/dist

.PHONY: help build build-go build-frontend tidy test run docker-up docker-down clean

help:
	@echo "EasyLAN web — make targets:"
	@echo "  make build           Build frontend + Go binary into ./$(BIN)"
	@echo "  make build-frontend  Build the Next.js static export"
	@echo "  make build-go        Build the Go binary (assumes embedded dist/ exists)"
	@echo "  make run             Run the binary against SQLite + memory cache"
	@echo "  make test            Run Go unit tests"
	@echo "  make docker-up       Start MySQL 5.7 + Redis via docker-compose"
	@echo "  make docker-down     Stop MySQL + Redis containers"
	@echo "  make clean           Remove build artefacts"

build: build-frontend build-go ## Build everything

build-frontend:
	cd $(FRONTEND_DIR) && $(NPM) install --no-audit --no-fund && $(NPM) run build
	rm -rf $(EMBED_DIR)
	cp -R $(FRONTEND_DIR)/out $(EMBED_DIR)

build-go:
	$(GO) build -o $(BIN) ./

tidy:
	$(GO) mod tidy

test:
	$(GO) test ./...

run: build-go
	./$(BIN)

docker-up:
	docker compose up -d

docker-down:
	docker compose down

clean:
	rm -f $(BIN)
	rm -rf $(EMBED_DIR)
	rm -rf $(FRONTEND_DIR)/.next $(FRONTEND_DIR)/out
