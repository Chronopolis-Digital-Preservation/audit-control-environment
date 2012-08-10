package edu.umiacs.ace.monitor.settings;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author shake
 */
@Entity
@Table(name = "system_settings")
@NamedQueries({
    @NamedQuery(name = "SettingsParameter.getAttr", query =
    "SELECT p FROM SettingsParameter p WHERE p.attr = :attr"),
    @NamedQuery(name = "SettingsParameter.getAttrList", query =
    "SELECT p FROM SettingsParameter p WHERE p.attr LIKE :attr"),
    @NamedQuery(name = "SettingsParameter.getCurrentSettings", query =
    "SELECT p FROM SettingsParameter p"),
    @NamedQuery(name = "SettingsParameter.getCustomSettings", query =
    "SELECT p FROM SettingsParameter p WHERE p.custom = TRUE")

})
public class SettingsParameter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String attr;
    private String value;
    private boolean custom;

    public SettingsParameter() {
        this.attr = null;
        this.value = null;
    }

    public SettingsParameter(String attr, String value, boolean custom) {
        this.attr = attr;
        this.value = value;
        this.custom = custom;
    }

    // Custom will default to false when added to DB unless otherwise specified
    public SettingsParameter(String attr, String value) {
       this.attr = attr;
       this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName(){
        return attr;
    }

    public boolean getCustom() {
        return custom;
    }

    public String getValue() {
        return value;
    }

    public void setAttribute(String attribute) {
        this.attr= attribute;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.settings.settingsParameter[id=" + id + "]";
    }
}
