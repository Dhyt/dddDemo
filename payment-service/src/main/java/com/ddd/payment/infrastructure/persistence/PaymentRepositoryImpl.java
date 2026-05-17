package com.ddd.payment.infrastructure.persistence;

import com.ddd.payment.domain.model.*;
import com.ddd.payment.domain.repository.PaymentRepository;
import com.ddd.payment.infrastructure.persistence.jpa.PaymentJpaEntity;
import com.ddd.payment.infrastructure.persistence.jpa.PaymentJpaRepository;
import com.ddd.payment.infrastructure.persistence.jpa.TransactionJpaEntity;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public void save(Payment payment) {
        PaymentJpaEntity entity = toJpa(payment);
        if (payment.getId() != null) {
            entity.setId(payment.getId().value());
        }
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId)
                .map(this::toDomain);
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        try {
            Payment payment = new Payment(entity.getOrderId(), entity.getAmount());
            payment.setId(entity.getId());

            if (entity.getMethod() != null) {
                payment.assignMethod(PaymentMethod.valueOf(entity.getMethod()));
            }

            Field statusField = Payment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(payment, PaymentStatus.valueOf(entity.getStatus()));

            Field transactionsField = Payment.class.getDeclaredField("transactions");
            transactionsField.setAccessible(true);
            List<Transaction> transactions = entity.getTransactions().stream()
                    .map(this::toDomainTransaction)
                    .collect(Collectors.toList());
            transactionsField.set(payment, transactions);

            Field createdAtField = Payment.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(payment, entity.getCreatedAt());

            return payment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Payment from JPA entity", e);
        }
    }

    private Transaction toDomainTransaction(TransactionJpaEntity entity) {
        Transaction transaction = new Transaction(
                Transaction.TransactionType.valueOf(entity.getType()),
                entity.getAmount(),
                Transaction.TransactionStatus.valueOf(entity.getStatus())
        );
        transaction.setTransactionId(new TransactionId(entity.getId()));
        return transaction;
    }

    private PaymentJpaEntity toJpa(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        if (payment.getId() != null) {
            entity.setId(payment.getId().value());
        }
        entity.setOrderId(payment.getOrderId());
        entity.setAmount(payment.getAmount());
        entity.setStatus(payment.getStatus().name());
        if (payment.getMethod() != null) {
            entity.setMethod(payment.getMethod().name());
        }
        entity.setCreatedAt(payment.getCreatedAt());

        List<TransactionJpaEntity> transactionEntities = payment.getTransactions().stream()
                .map(t -> toJpaTransaction(t, entity))
                .toList();
        entity.setTransactions(transactionEntities);

        return entity;
    }

    private TransactionJpaEntity toJpaTransaction(Transaction transaction, PaymentJpaEntity paymentEntity) {
        TransactionJpaEntity entity = new TransactionJpaEntity();
        if (transaction.getTransactionId() != null) {
            entity.setId(transaction.getTransactionId().value());
        }
        entity.setType(transaction.getType().name());
        entity.setAmount(transaction.getAmount());
        entity.setStatus(transaction.getStatus().name());
        entity.setCreatedAt(transaction.getCreatedAt());
        entity.setPayment(paymentEntity);
        return entity;
    }
}
