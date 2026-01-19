package uk.bit1.spring_jpa.repository.projection;

public interface CustomerWithOrderCount {
    Long getCustomerId();
    String getFirstName();
    String getLastName();
    long getOrderCount();
}
