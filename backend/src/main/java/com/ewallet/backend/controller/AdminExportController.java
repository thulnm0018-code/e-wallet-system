package com.ewallet.backend.controller;

import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/export")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExportController {

    private final TransactionRepository transactionRepository;

    public AdminExportController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transactions")
    public void exportTransactionsToCsv(
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=transactions_report.csv"
        );

        PrintWriter writer = response.getWriter();

        // BOM để Excel đọc tiếng Việt đúng
        writer.write('\ufeff');

        writer.println(
                "Transaction Code,Sender Phone,Receiver Phone,Amount,Type,Status,Created At"
        );

        List<Transaction> transactions =
                transactionRepository.findAllForExport();

        for (Transaction tx : transactions) {

            String senderPhone =
                    tx.getSenderWallet() != null
                            && tx.getSenderWallet().getUser() != null
                            ? tx.getSenderWallet().getUser().getPhone()
                            : "N/A";

            String receiverPhone =
                    tx.getReceiverWallet() != null
                            && tx.getReceiverWallet().getUser() != null
                            ? tx.getReceiverWallet().getUser().getPhone()
                            : "N/A";

            String amount =
                    tx.getAmount() != null
                            ? tx.getAmount().toPlainString()
                            : "";

            String type =
                    tx.getType() != null
                            ? tx.getType().name()
                            : "";

            String status =
                    tx.getStatus() != null
                            ? tx.getStatus().name()
                            : "";

            String createdAt =
                    tx.getCreatedAt() != null
                            ? tx.getCreatedAt().toString()
                            : "";

            writer.println(String.join(",",
                    safeCsv(tx.getTransactionCode()),
                    safeCsv(senderPhone),
                    safeCsv(receiverPhone),
                    amount,
                    type,
                    status,
                    createdAt
            ));
        }

        writer.flush();
    }

    /**
     * Ngăn CSV Injection
     * Escape dấu "
     * Bao giá trị trong dấu "
     */
    private String safeCsv(String value) {

        if (value == null) {
            return "";
        }

        if (value.startsWith("=")
                || value.startsWith("+")
                || value.startsWith("-")
                || value.startsWith("@")) {

            value = "'" + value;
        }

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }
}