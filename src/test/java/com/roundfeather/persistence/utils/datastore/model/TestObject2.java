package com.roundfeather.persistence.utils.datastore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestObject2 {

    double d;
    int i;
    String s;
    float f;
    long l;
    List<Double> ds;
    Map<String, Float> fs;
    Boolean b1;
}
