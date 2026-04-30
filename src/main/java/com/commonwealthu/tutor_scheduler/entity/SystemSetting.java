package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"System_Settings\"")
public class SystemSetting {

    @Id
    @Column(name = "\"SettingKey\"")
    private String key;

    @Column(name = "\"SettingValue\"")
    private boolean value;

    public SystemSetting() {}

    public SystemSetting(String key, boolean value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public boolean isValue() { return value; }
    public void setValue(boolean value) { this.value = value; }
}