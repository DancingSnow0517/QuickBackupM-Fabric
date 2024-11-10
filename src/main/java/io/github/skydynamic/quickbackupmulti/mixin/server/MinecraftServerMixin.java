package io.github.skydynamic.quickbackupmulti.mixin.server;

import io.github.skydynamic.quickbackupmulti.QbmConstant;
import io.github.skydynamic.quickbackupmulti.QuickBackupMulti;
import io.github.skydynamic.quickbackupmulti.utils.QbmManager;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.getDataBase;
import static io.github.skydynamic.quickbackupmulti.QuickBackupMulti.setDataStore;
import static io.github.skydynamic.quickbackupmulti.utils.QbmManager.createBackupDir;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.shutdownSchedule;
import static io.github.skydynamic.quickbackupmulti.utils.schedule.ScheduleUtils.startSchedule;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract Path getSavePath(WorldSavePath worldSavePath);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setServer(CallbackInfo ci) {
        QuickBackupMulti.TEMP_CONFIG.setServerValue((MinecraftServer)(Object)this);
    }

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void initQuickBackupMulti(CallbackInfo ci) {
        Path backupDir = Path.of(QbmConstant.pathGetter.getGamePath() + "/QuickBackupMulti/");
        QuickBackupMulti.TEMP_CONFIG.setWorldName("");
        QbmManager.savePath = this.getSavePath(WorldSavePath.ROOT);
        createBackupDir(backupDir);
        setDataStore("server");
        startSchedule();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void injectShutDown(CallbackInfo ci) {
        shutdownSchedule();
        if (!QuickBackupMulti.TEMP_CONFIG.isBackup) getDataBase().stopInternalMongoServer();
    }
}
