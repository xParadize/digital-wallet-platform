FROM golang:1.22-alpine AS builder

WORKDIR /app

COPY otp-service/go.mod otp-service/go.sum ./
RUN go mod download

COPY otp-service/. ./
COPY .env .env

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o otp-service ./cmd/main.go

FROM alpine:latest
WORKDIR /app

RUN apk --no-cache add ca-certificates

COPY --from=builder /app/otp-service .
COPY --from=builder /app/.env .env

EXPOSE 8100
CMD ["./otp-service"]
