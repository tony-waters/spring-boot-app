package repository.projection;

public interface CustomerWithOrderCount {
    Long getCustomerId();
    String getFirstName();
    String getLastName();
    long getOrderCount();
}
