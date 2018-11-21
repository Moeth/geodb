package de.neofonie.osm.reader.street;

import com.google.common.collect.ImmutableSet;
import de.neofonie.osm.reader.street.AddressBean.AddressBeanBuilder;
import de.neofonie.osm.util.TagUtil;
import lombok.RequiredArgsConstructor;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class AddressExtractor implements Consumer<Entity> {

    private static final Logger log = LoggerFactory.getLogger(AddressExtractor.class);
    public static final String ADDR_CITY = "addr:city";
    public static final String ADDR_STREET = "addr:street";
    public static final String ADDR_SUBURB = "addr:suburb";
    public static final String ADDR_COUNTRY = "addr:country";
    public static final String ADDR_POSTCODE = "addr:postcode";
    public static final String ADDR_HOUSENUMBER = "addr:housenumber";
    public static final ImmutableSet<String> ADDRESS_FIELDS = ImmutableSet.of(ADDR_CITY, ADDR_STREET, ADDR_SUBURB, ADDR_COUNTRY, ADDR_POSTCODE, ADDR_HOUSENUMBER);

    private final Consumer<AddressBean> addressBeanConsumer;
//    private int i;

    @Override
    public void accept(final Entity entity) {

        Collection<Tag> tags = entity.getTags();
        if (TagUtil.containsAll(tags, ADDRESS_FIELDS)) {
            AddressBeanBuilder builder = AddressBean.builder();
            TagUtil.getTagValue(tags, ADDR_CITY).ifPresent(builder::city);
            TagUtil.getTagValue(tags, ADDR_STREET).ifPresent(builder::street);
            TagUtil.getTagValue(tags, ADDR_SUBURB).ifPresent(builder::suburb);
            TagUtil.getTagValue(tags, ADDR_COUNTRY).ifPresent(builder::country);
            TagUtil.getTagValue(tags, ADDR_POSTCODE).ifPresent(builder::postcode);
            TagUtil.getTagValue(tags, ADDR_HOUSENUMBER).ifPresent(builder::housenumber);
            addressBeanConsumer.accept(builder.build());
//        } else if (TagUtil.containsAny(tags, ADDRESS_FIELDS)) {
//            int f = i++;
//            log.info(f+" "+TagUtil.toString(tags));
        }
    }
}
