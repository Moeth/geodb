package de.neofonie.osm.reader.street;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
@Data
public class AddressBean {

    private static final Logger log = LoggerFactory.getLogger(AddressBean.class);
    @NonNull
    private String street;
    private String housenumber;
    @NonNull
    private String postcode;
    @NonNull
    private String city;
    private String suburb;
    @NonNull
    private String country;
}
