package dev.dosya.sdk.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A page of an endpoint's delivery log, newest first.
 *
 * @since 0.3.0
 */
public final class WebhookDeliveriesResponse {

    private List<WebhookDelivery> deliveries;
    private Page pagination;

    private WebhookDeliveriesResponse() {}

    public @NotNull List<WebhookDelivery> getDeliveries() {
        return deliveries == null ? Collections.emptyList() : Collections.unmodifiableList(deliveries);
    }

    public @NotNull Page getPagination() {
        return pagination != null ? pagination : new Page();
    }

    /** Pagination of the delivery log. */
    public static final class Page {
        private int page;
        private int perPage;
        private int total;
        private int totalPages;

        private Page() {}

        /** 1-based page number. */
        public int getPage() { return page; }
        public int getPerPage() { return perPage; }
        public int getTotal() { return total; }
        /** At least 1. */
        public int getTotalPages() { return totalPages; }
    }
}
