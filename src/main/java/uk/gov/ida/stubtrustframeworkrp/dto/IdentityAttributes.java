package uk.gov.ida.stubtrustframeworkrp.dto;


public class IdentityAttributes {

    private String birthdate;

    private Address address;

    private String gender;

    private String name;

    private String given_name;

    private String middle_name;

    private String family_name;

    private String ho_positive_verification_notice;

    private String bank_account_number;

    public IdentityAttributes() {
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public String getHo_positive_verification_notice() {
        return ho_positive_verification_notice;
    }

    public String getBank_account_number() {
        return bank_account_number;
    }

    public Address getAddress() {
        return address;
    }
}
