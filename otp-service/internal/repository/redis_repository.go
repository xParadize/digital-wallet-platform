package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"otp-service/internal/config"
	"otp-service/internal/models"

	"github.com/go-redis/redis/v8"
	"github.com/google/uuid"
)

type RedisRepository struct {
	client *redis.Client
	config *config.Config
	ctx    context.Context
}

func NewRedisRepository(cfg *config.Config) *RedisRepository {
	rdb := redis.NewClient(&redis.Options{
		Addr:     cfg.RedisAddr,
		Password: cfg.RedisPass,
		DB:       cfg.RedisDB,
	})

	ctx := context.Background()

	_, err := rdb.Ping(ctx).Result()
	if err != nil {
		panic("Failed to connect to Redis: " + err.Error())
	}

	return &RedisRepository{
		client: rdb,
		config: cfg,
		ctx:    ctx,
	}
}

func (r *RedisRepository) GetOtpKey(userID uuid.UUID, offerID string) string {
	return fmt.Sprintf("otp:user:%s:offer:%s", userID.String(), offerID)
}

func (r *RedisRepository) GetResendCounterKey(userID uuid.UUID, offerID string) string {
	return fmt.Sprintf("otp:resend:user:%s:offer:%s", userID.String(), offerID)
}

func (r *RedisRepository) StoreOtp(userID uuid.UUID, offerID string, otpCode string) error {
	key := r.GetOtpKey(userID, offerID)

	resendCount, _ := r.GetResendCount(userID, offerID)

	otpData := models.OtpData{
		Code:     otpCode,
		UserID:   userID.String(),
		OfferID:  offerID,
		Attempts: 0,
		Resends:  resendCount,
	}

	data, err := json.Marshal(otpData)
	if err != nil {
		return err
	}

	return r.client.Set(r.ctx, key, data, time.Duration(r.config.OtpTTL)*time.Second).Err()
}

func (r *RedisRepository) GetOtp(userID uuid.UUID, offerID string) (*models.OtpData, error) {
	key := r.GetOtpKey(userID, offerID)

	data, err := r.client.Get(r.ctx, key).Result()
	if err != nil {
		if err == redis.Nil {
			return nil, fmt.Errorf("OTP not found or expired")
		}
		return nil, err
	}

	var otpData models.OtpData
	err = json.Unmarshal([]byte(data), &otpData)
	if err != nil {
		return nil, err
	}

	return &otpData, nil
}

func (r *RedisRepository) UpdateOtpAttempts(userID uuid.UUID, offerID string, attempts int) error {
	otpData, err := r.GetOtp(userID, offerID)
	if err != nil {
		return err
	}

	otpData.Attempts = attempts

	data, err := json.Marshal(otpData)
	if err != nil {
		return err
	}

	key := r.GetOtpKey(userID, offerID)
	return r.client.Set(r.ctx, key, data, r.client.TTL(r.ctx, key).Val()).Err()
}

func (r *RedisRepository) DeleteOtp(userID uuid.UUID, offerID string) error {
	key := r.GetOtpKey(userID, offerID)
	return r.client.Del(r.ctx, key).Err()
}

func (r *RedisRepository) IncrementResendCount(userID uuid.UUID, offerID string) (int, error) {
	key := r.GetResendCounterKey(userID, offerID)
	count, err := r.client.Incr(r.ctx, key).Result()
	if err != nil {
		return 0, err
	}

	r.client.Expire(r.ctx, key, 60*time.Second)

	return int(count), nil
}

func (r *RedisRepository) GetResendCount(userID uuid.UUID, offerID string) (int, error) {
	key := r.GetResendCounterKey(userID, offerID)
	count, err := r.client.Get(r.ctx, key).Int()
	if err != nil {
		if err == redis.Nil {
			return 0, nil
		}
		return 0, err
	}
	return count, nil
}

func (r *RedisRepository) ResetResendCount(userID uuid.UUID, offerID string) error {
	key := r.GetResendCounterKey(userID, offerID)
	return r.client.Del(r.ctx, key).Err()
}

func (r *RedisRepository) Close() error {
	return r.client.Close()
}