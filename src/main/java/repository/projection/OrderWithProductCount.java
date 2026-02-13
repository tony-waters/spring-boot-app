package repository.projection;

public interface OrderWithProductCount {
    Long getOrderId();
    String getDescription();
    boolean isFulfilled();
    long getProductCount();
}
