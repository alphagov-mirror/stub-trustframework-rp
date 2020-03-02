package uk.gov.ida.stubtrustframeworkrp.dto;


public class IdentityAttributes {

    private String birthdate;

    private String gender;

    private String name;

    private String given_name;

    private String middle_name;

    private String family_name;

    private String ho_positive_verification_notice;


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
}
