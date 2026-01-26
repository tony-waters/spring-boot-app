package uk.bit1.spring_jpa.entity;

import jakarta.persistence.*;

@Entity
public class ContactInfo extends BaseEntity {

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    protected ContactInfo() {}

    public ContactInfo(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Customer getCustomer() {
        return customer;
    }

    protected void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactInfo other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
