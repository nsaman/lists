package com.lists.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nick on 10/28/2018.
 */

public class DateDescriptor extends Descriptor {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private Date value;

    public DateDescriptor() {
        super(DescriptorTypes.DATE);
    }

    @Override
    public String getReadableString() {
        return value!=null?value.toString():"";
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public static Date formatDate(String string) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(string);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Could not convert to " + DATE_FORMAT + " from value=" + string, ex);
        }
    }
}
