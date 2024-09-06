package com.hbm.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import net.minecraftforge.fml.common.Loader;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null; // 使用默认的 refmap
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.equals("com.hbm.mixin.MixinTileEntityFluidTank")) {
            return Loader.isModLoaded("mekanism");
        }
        // if (mixinClassName.equals("com.hbm.mixin.MixinTileFaucet")) {
        //     return Loader.isModLoaded("tconstruct");
        // }
        if (mixinClassName.equals("MixinComposterBlock$ItemsForComposter")) {
            return Loader.isModLoaded("futuremc");
        }
        return true; // 其他 Mixin 默认加载
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null; // 返回 null，使用 JSON 文件中列出的 mixin 列表
    }


    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
