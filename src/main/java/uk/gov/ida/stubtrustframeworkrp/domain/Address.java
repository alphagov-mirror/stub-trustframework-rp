package uk.gov.ida.stubtrustframeworkrp.domain;

public class Address {

    private String street;
    private String postCode;
    private String county;
    private String country;
    private String town;
    private String type;

    public String getStreet() {
        return street;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCounty() {
        return county;
    }

    public String getCountry() {
        return country;
    }

    public String getTown() {
        return town;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", postCode='" + postCode + '\'' +
                ", county='" + county + '\'' +
                ", country='" + country + '\'' +
                ", town='" + town + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
