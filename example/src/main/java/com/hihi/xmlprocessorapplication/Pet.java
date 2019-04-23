package com.hihi.xmlprocessorapplication;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.hihi.xml.XmlPath;

@AutoValue
abstract class Pet {

    static final String XML =
            "<?xml version=\"1.0\"?>\n" +
                    "<pets>\n" +
                    "   <item id=\"PetRequestId\">\n" +
                    "       <name>Meatball</name>\n" +
                    "       <breed>Persian</breed>\n" +
                    "       <age>\n" +
                    "           <years>3</years>\n" +
                    "           <months>4</months>\n" +
                    "       </age>\n" +
                    "       <favourite>true</favourite>\n" +
                    "       <description color=\"white\"/>\n" +
                    "   </item>\n" +
                    "</pets>";

    @XmlPath("/pets/item/@id")
    abstract String id();

    @XmlPath("/pets/item/name/text()")
    abstract String name();

    @XmlPath("/pets/item/breed/text()")
    abstract String breed();

    @XmlPath("/pets/item/age/years/text()")
    abstract int years();

    @XmlPath("/pets/item/age/months/text()")
    abstract int months();

    @XmlPath("/pets/item/age/days/text()")
    @Nullable
    abstract Integer days();

    @XmlPath("/pets/item/favourite/text()")
    abstract boolean favourite();

    @XmlPath("/pets/item/description/@color")
    abstract String color();

    @XmlPath("/pets/item/description/text()")
    @Nullable
    abstract String description();

    static Pet fromXml(String xml) {
        return AutoValue_Pet.createFromXml(xml);
    }

}
