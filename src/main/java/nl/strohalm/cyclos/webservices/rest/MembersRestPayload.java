
package nl.strohalm.cyclos.webservices.rest;

import nl.strohalm.cyclos.entities.members.imports.MemberImport;
import nl.strohalm.cyclos.utils.StringValuedEnum;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;

/**
 * Contains temporary data for an json payload
 * 
 * @author serge
 */
public class MembersRestPayload {


    public static enum Status implements StringValuedEnum {
        SUCCESS, MISSING_NAME, MISSING_USERNAME, INVALID_USERNAME, USERNAME_ALREADY_IN_USE, MISSING_EMAIL,
        INVALID_EMAIL, INVALID_CREATION_DATE, MISSING_CUSTOM_FIELD, INVALID_CUSTOM_FIELD, INVALID_BALANCE,
        BALANCE_LOWER_THAN_CREDIT_LIMIT, BALANCE_UPPER_THAN_CREDIT_LIMIT, INVALID_CREDIT_LIMIT,
        INVALID_UPPER_CREDIT_LIMIT, INVALID_RECORD_TYPE, INVALID_RECORD_TYPE_FIELD, MISSING_RECORD_FIELD,
        INVALID_RECORD_FIELD, UNKNOWN_ERROR, INVALID_CUSTOM_FIELD_VALUE_UNIQUE,
        INVALID_CUSTOM_FIELD_VALUE_MAX_LENGTH, INVALID_CUSTOM_FIELD_VALUE_MIN_LENGTH;
        @Override
        public String getValue() {
            return name();
        }
    }

    private static final long                  serialVersionUID = -4080042034080488479L;
    private MemberImport                       _import;
    private Status                             status;
    private String                             errorArgument1;
    private String                             errorArgument2;
    private String                             name;
    private String                             salt;
    private String                             username;
    private String                             password;
    private String                             email;
    private Integer                            lineNumber;
    private Calendar                           creationDate;
    private BigDecimal                         creditLimit;
    private BigDecimal                         upperCreditLimit;
    private BigDecimal                         initialBalance;

    public Calendar getCreationDate() {
        return creationDate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getEmail() {
        return email;
    }

    public String getErrorArgument1() {
        return errorArgument1;
    }

    public String getErrorArgument2() {
        return errorArgument2;
    }

    public MemberImport getImport() {
        return _import;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getUpperCreditLimit() {
        return upperCreditLimit;
    }

    public String getUsername() {
        return username;
    }

    public void setCreationDate(final Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreditLimit(final BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setErrorArgument1(final String errorArgument) {
        errorArgument1 = errorArgument;
    }

    public void setErrorArgument2(final String errorArgument2) {
        this.errorArgument2 = errorArgument2;
    }

    public void setImport(final MemberImport _import) {
        this._import = _import;
    }

    public void setInitialBalance(final BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public void setLineNumber(final Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    public void setStatus(final Status status) {
        this.status = status;
        if (status != null && status != Status.SUCCESS) {
            creditLimit = BigDecimal.ZERO;
            initialBalance = null;
            upperCreditLimit = null;
        }
    }

    public void setUpperCreditLimit(final BigDecimal upperCreditLimit) {
        this.upperCreditLimit = upperCreditLimit;
    }

    public void setUsername(final String username) {
        this.username = username;
    }


}
