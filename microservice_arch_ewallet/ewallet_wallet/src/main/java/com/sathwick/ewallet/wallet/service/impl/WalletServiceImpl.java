package com.sathwick.ewallet.wallet.service.impl;

import com.sathwick.ewallet.wallet.domain.Wallet;
import com.sathwick.ewallet.wallet.exception.WalletException;
import com.sathwick.ewallet.wallet.repository.WalletRepository;
import com.sathwick.ewallet.wallet.service.WalletService;
import com.sathwick.ewallet.wallet.service.resource.WalletResponse;
import com.sathwick.ewallet.wallet.service.resource.WalletTransactionRequest;
import com.sathwick.ewallet.wallet.util.TransactionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {

    private WalletRepository walletRepository;
    @Getter
    private KafkaTemplate kafkaTemplate;

    @Autowired
    public void setWalletRepository(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void createWallet(Long userId) {
        try{
            Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
            if(optionalWallet.isPresent()){
                // happens when the mode is at least once in a kafka topic then duplicate wallet might be created
                // to avoid we must return the flow
                log.info("Wallet already exists and assigned to user with id: "+userId);
                return;
            }
            walletRepository.save(Wallet.builder().userId(userId).balance(0.0).active(true).build());
        }
        catch (Exception e){
            log.error("Exception while creating wallet: {}", e.getMessage());
//            throw new WalletException("EWALLET_WALLET_CREATION_FAILED","Exception while creating wallet");
        }
    }

    @Override
    public Wallet deleteWallet(Long userId) {
        try{
            Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet not found"));
            walletRepository.delete(wallet);
            return wallet;
        }
        catch (Exception e){
            log.error("Exception while deleting wallet: {}", e.getMessage());
//            throw new WalletException("EWALLET_WALLET_DELETION_FAILED", "Exception while deleting wallet");
        }
        return null;
    }

    @Override
    public WalletResponse getWallet(Long userId) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        if(optionalWallet.isEmpty()){
            log.error("Wallet for user "+userId+" Not found");
            throw new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet for user "+userId+" not found");
        }
        return new WalletResponse(optionalWallet.get());
    }

//    @Override
//    public boolean performTransaction(WalletTransactionRequest walletTransactionRequest) {
//        /*
//         * TODO: Implement this method
//         *  1. fetch the wallet of the senderUserId and validate not null
//         *  2. make sure amount is <= walletOfUserId amount : Is eligible for transaction
//         *  3. fetch the wallet of the receiverUserId and validate not null
//         *  4. Update the balances and repository
//         */
//        log.info("Performing transaction from : "+walletTransactionRequest.getSenderId()+" to: "+walletTransactionRequest.getReceiverId());
//        Wallet senderWallet = walletRepository.findByUserId(walletTransactionRequest.getSenderId()).orElseThrow(() -> new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet not found for user "+walletTransactionRequest.getSenderId()));
//        Wallet receiverWallet = walletRepository.findByUserId(walletTransactionRequest.getReceiverId()).orElseThrow(() -> new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet not found for user "+walletTransactionRequest.getReceiverId()));
//        if(senderWallet.getBalance() < walletTransactionRequest.getAmount()){
//            throw new WalletException("EWALLET_INSUFFICIENT_BALANCE", "Insufficient Balance in sender account");
//        }
//        senderWallet.setBalance(senderWallet.getBalance()- walletTransactionRequest.getAmount());
//        receiverWallet.setBalance(receiverWallet.getBalance()+ walletTransactionRequest.getAmount());
//        walletRepository.save(senderWallet);
//        // what happens if there is an exception in the middle of the system.
//        walletRepository.save(receiverWallet);
//        return true;
//    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = WalletException.class, noRollbackFor = NullPointerException.class)
    public boolean performTransaction(WalletTransactionRequest walletTransactionRequest) {
        // Transaction with Transactional Annotation
        try {
            log.info("Performing transaction from : "+walletTransactionRequest.getSenderId()+" to: "+walletTransactionRequest.getReceiverId());
            Wallet senderWallet = walletRepository.findByUserId(walletTransactionRequest.getSenderId()).orElseThrow(() -> new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet not found for user "+walletTransactionRequest.getSenderId()));
            Wallet receiverWallet = walletRepository.findByUserId(walletTransactionRequest.getReceiverId()).orElseThrow(() -> new WalletException("EWALLET_WALLET_NOT_FOUND", "Wallet not found for user "+walletTransactionRequest.getReceiverId()));
            if(TransactionType.DEPOSIT.name().equals(walletTransactionRequest.getTransactionType())){
                // update wallet
                updateWallet(receiverWallet, walletTransactionRequest.getAmount());
                return true;
            }
            if(TransactionType.WITHDRAW.name().equals(walletTransactionRequest.getTransactionType())){
                // update wallet
                updateWallet(receiverWallet, -walletTransactionRequest.getAmount());
                return true;
            }
            if(TransactionType.TRANSFER.name().equals(walletTransactionRequest.getTransactionType())) {
                if (senderWallet.getBalance() < walletTransactionRequest.getAmount()) {
                    throw new WalletException("EWALLET_INSUFFICIENT_BALANCE", "Insufficient Balance in sender account");
                }
                senderWallet.setBalance(senderWallet.getBalance() - walletTransactionRequest.getAmount());
                receiverWallet.setBalance(receiverWallet.getBalance() + walletTransactionRequest.getAmount());
                walletRepository.save(senderWallet);
                walletRepository.save(receiverWallet);
                log.info("Updated wallets: Transaction successful");
                return true;
            }
        } catch (Exception e){
            log.error("Exception while performing transaction: {}", e.getMessage());
        }
        return false;
    }

    private void updateWallet(Wallet receiverWallet, Double amount) {
        receiverWallet.setBalance(receiverWallet.getBalance()+amount);
        walletRepository.save(receiverWallet);
    }

    @Autowired
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
}
