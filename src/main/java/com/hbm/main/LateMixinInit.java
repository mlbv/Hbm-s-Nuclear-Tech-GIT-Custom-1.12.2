package com.hbm.main;

import java.util.Arrays;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

public class LateMixinInit implements ILateMixinLoader{
    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.hbm_late.json");
    }
}