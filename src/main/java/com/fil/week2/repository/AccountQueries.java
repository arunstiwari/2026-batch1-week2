package com.fil.week2.repository;

import com.fil.week2.dto.AccountSummary;
import com.fil.week2.model.AccountNumber;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The query side. Accounts are not an aggregate root, so this is not a
 * {@code JpaRepository}: extending it would inherit save() and findById(), handing
 * out a write path around the Customer root. Marker {@code Repository} lets us
 * declare only what we want, and nothing here returns an Account.
 */
public interface AccountQueries extends Repository<com.fil.week2.model.Account, Long> {

    @Query("""
            select new com.fil.week2.dto.AccountSummary(
                a.accountNumber, a.accountType, a.balance, a.createdAt)
            from Account a
            where a.customer.id = :customerId
            order by a.accountNumber
            """)
    List<AccountSummary> findSummaries(@Param("customerId") Long customerId);

    boolean existsByAccountNumber(AccountNumber accountNumber);
}
