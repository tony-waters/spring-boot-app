package uk.bit1.spring_jpa.repository.projection;

public interface OrderWithProductCount {
    Long getOrderId();
    String getDescription();
    boolean isFulfilled();
    long getProductCount();
}
