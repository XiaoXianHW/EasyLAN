"""Execute the real 1.21.11 bridge and mixin bodies, including negative controls."""
from pathlib import Path
import json
import unittest
from test_lan_publication import run_java

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'versions/1.21.11/project/src/main/java'
BASE = 'org/xiaoxian/easylan/fabric'

BRIDGE_STUBS = {
 'net/minecraft/client/server/IntegratedServer.java': '''package net.minecraft.client.server;
import org.xiaoxian.easylan.fabric.version.ConfiguredPlayerLimit;
public class IntegratedServer implements ConfiguredPlayerLimit {
  int limit=8; public boolean wrongAdmission;
  public class PlayerList { public int getMaxPlayers(){return wrongAdmission ? 8 : IntegratedServer.this.getMaxPlayers();} }
  private final PlayerList list=new PlayerList();
  public PlayerList getPlayerList(){return list;}
  public int getMaxPlayers(){return limit;}
  public void easylan$setMaxPlayers(int value){limit=value;}
}''',
 f'{BASE}/version/ReflectionVersionBridgeSupport.java': f'''package org.xiaoxian.easylan.fabric.version;
public abstract class ReflectionVersionBridgeSupport {{
  public boolean setMaxPlayers(Object s,int n){{throw new AssertionError("obsolete reflection path");}}
  protected abstract String[] maxPlayerFieldNames();
}}''',
 'BridgeHarness.java': '''import net.minecraft.client.server.IntegratedServer;
import org.xiaoxian.easylan.fabric.version.VersionBridgeImpl;
public class BridgeHarness { public static void main(String[] args) {
  VersionBridgeImpl bridge=new VersionBridgeImpl(); IntegratedServer server=new IntegratedServer(); Object original=server.getPlayerList();
  if (!bridge.setMaxPlayers(server,99) || server.getMaxPlayers()!=99 || server.getPlayerList().getMaxPlayers()!=99)
    throw new AssertionError("Server and admission must share the applied limit");
  if (server.getPlayerList()!=original) throw new AssertionError("Do not replace live player list");
  if (bridge.setMaxPlayers(server,1) || bridge.setMaxPlayers(server,500001) || server.getMaxPlayers()!=99)
    throw new AssertionError("Reject invalid limit");
  if (bridge.setMaxPlayers(new Object(),99)) throw new AssertionError("Reject untransformed server");
  IntegratedServer second=new IntegratedServer();
  if(second.getPlayerList().getMaxPlayers()!=8) throw new AssertionError("New-world leak");
  second.wrongAdmission=true;
  if(bridge.setMaxPlayers(second,99)) throw new AssertionError("Configured getter must not mask wrong admission cap");
} }'''
}
MIXIN_STUBS = {
 'net/minecraft/client/server/IntegratedServer.java': 'package net.minecraft.client.server; public class IntegratedServer {}',
 'org/spongepowered/asm/mixin/Mixin.java': 'package org.spongepowered.asm.mixin; public @interface Mixin { Class<?> value(); boolean remap() default true; }',
 'org/spongepowered/asm/mixin/Unique.java': 'package org.spongepowered.asm.mixin; public @interface Unique {}',
 'org/spongepowered/asm/mixin/injection/At.java': 'package org.spongepowered.asm.mixin.injection; public @interface At { String value(); }',
 'org/spongepowered/asm/mixin/injection/Inject.java': 'package org.spongepowered.asm.mixin.injection; public @interface Inject { String method(); At at(); boolean cancellable(); boolean remap() default true; }',
 'org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable.java': '''package org.spongepowered.asm.mixin.injection.callback;
public class CallbackInfoReturnable<T> { private T value; public CallbackInfoReturnable(T v){value=v;}
public void setReturnValue(T v){value=v;} public T getReturnValue(){return value;} }''',
 'MixinHarness.java': '''import org.xiaoxian.easylan.fabric.mixin.IntegratedServerMixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
public class MixinHarness extends IntegratedServerMixin {
 int effective() throws Exception {
   CallbackInfoReturnable<Integer> callback=new CallbackInfoReturnable<Integer>(8);
   java.lang.reflect.Method method=IntegratedServerMixin.class.getDeclaredMethod("easylan$resolveMaxPlayers",CallbackInfoReturnable.class);
   method.setAccessible(true);method.invoke(this,callback);return callback.getReturnValue();
 }
 public static void main(String[] args) throws Exception {
   MixinHarness first=new MixinHarness(); if(first.effective()!=8)throw new AssertionError("Preserve default");
   first.easylan$setMaxPlayers(99);if(first.effective()!=99)throw new AssertionError("Actual getter must return applied limit");
   first.easylan$setMaxPlayers(37);if(first.effective()!=37)throw new AssertionError("Repeated change");
   if(new MixinHarness().effective()!=8)throw new AssertionError("Limit leaked across worlds");
 }
}'''
}

class PlayerLimit12111(unittest.TestCase):
    def test_bridge_reads_actual_admission_limit(self):
        files=dict(BRIDGE_STUBS)
        for name in ('VersionBridgeImpl','ConfiguredPlayerLimit'):
            rel=f'{BASE}/version/{name}.java'
            files[rel]=(JAVA/rel).read_text()
        result=run_java(files,'BridgeHarness')
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)
        rel=f'{BASE}/version/VersionBridgeImpl.java'
        files[rel]=files[rel].replace('configured.easylan$setMaxPlayers(maxPlayers);','')
        result=run_java(files,'BridgeHarness')
        self.assertNotEqual(result.returncode,0,'Negative control missed unapplied player limit')

    def test_mixin_is_per_server_and_negative_control(self):
        files=dict(MIXIN_STUBS)
        for rel in (f'{BASE}/mixin/IntegratedServerMixin.java',f'{BASE}/version/ConfiguredPlayerLimit.java'):
            files[rel]=(JAVA/rel).read_text()
        result=run_java(files,'MixinHarness')
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)
        rel=f'{BASE}/mixin/IntegratedServerMixin.java'
        self.assertNotIn('CustomMaxPlayer',files[rel])
        files[rel]=files[rel].replace('callback.setReturnValue(easylan$maxPlayers);','')
        result=run_java(files,'MixinHarness')
        self.assertNotEqual(result.returncode,0,'Negative control missed hardcoded limit')

    def test_required_remappable_mixin_registration(self):
        resources=JAVA.parent/'resources'
        config=json.loads((resources/'easylan.mixins.json').read_text())
        self.assertTrue(config['required'])
        self.assertEqual(config['injectors']['defaultRequire'],1)
        self.assertIn('IntegratedServerMixin',config['client'])
        mod=json.loads((resources/'fabric.mod.json').read_text())
        self.assertIn('easylan.mixins.json',mod['mixins'])
        source=(JAVA/f'{BASE}/mixin/IntegratedServerMixin.java').read_text()
        self.assertNotIn('remap = false',source)

if __name__ == '__main__':
    unittest.main()
