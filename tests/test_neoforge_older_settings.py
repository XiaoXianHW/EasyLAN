"""Check inherited vanilla publication and mapped older server/player-list values."""
from pathlib import Path
import unittest

from test_neoforge_12111_settings import ROOT, START_HARNESS, BRIDGE_FILES, method, run_java

GROUP = ROOT / 'shared/neoforge-group/src/main/java'
GUI_FILES = [ROOT / 'shared/neoforge-modern/src/main/java/org/xiaoxian/gui/GuiShareToLanEdit.java',
             ROOT / 'versions/1.20.1/project/src/main/java/org/xiaoxian/gui/GuiShareToLanEdit.java']

INHERITED_HARNESS = r'''
public class InheritedHarness {
    static class EditBox { String value; EditBox(String v){value=v;} String getValue(){return value;} void setValue(String v){value=v;} }
    static class IntegratedServer { boolean published; boolean fail; int port=-1; boolean isPublished(){return published;} }
    static class Minecraft {
        static Minecraft INSTANCE=new Minecraft(); IntegratedServer server=new IntegratedServer();
        static Minecraft getInstance(){return INSTANCE;} IntegratedServer getSingleplayerServer(){return server;}
    }
    static class Button { Runnable action; Button(Runnable r){action=r;} void onPress(){action.run();} }
    static class I18n { static String get(String key){return key;} }
    static class ChatUtil { static int errors; static void sendMsg(String value){errors++;} }
    static class ConfigUtil { static int saves; static void save(){saves++;} }
    static class ShareToLan { static int setups; void handleLanSetup(){setups++;} }
    EditBox vanillaPortTextBox=new EditBox(""), PortTextBox=new EditBox("25599"), MaxPlayerBox=new EditBox("99");
    String PortText,MaxPlayerText,CustomPort,CustomMaxPlayer;
    SYNC_METHOD
    PRODUCTION_METHOD
    Button original() { return new Button(() -> {
        var server=Minecraft.getInstance().server;
        if(!server.fail) {
            server.port=vanillaPortTextBox.value.isEmpty()?37563:Integer.parseInt(vanillaPortTextBox.value);
            server.published=true;
        }
    }); }
    public static void main(String[] args) {
        for(String input:new String[]{"25599",""}) {
            Minecraft.INSTANCE=new Minecraft();var h=new InheritedHarness();h.PortTextBox.setValue(input);
            h.startLan(h.original());int expected=input.isEmpty()?37563:25599;
            if(Minecraft.getInstance().server.port!=expected)throw new AssertionError("Vanilla responder must receive custom port");
        }
        Minecraft.INSTANCE=new Minecraft();var failed=new InheritedHarness();Minecraft.INSTANCE.server.fail=true;
        int count=ShareToLan.setups;failed.startLan(failed.original());
        if(count!=ShareToLan.setups)throw new AssertionError("Do not setup/report success after bind failure");
        var missing=new InheritedHarness();missing.vanillaPortTextBox=null;
        missing.startLan(new Button(()->{throw new AssertionError("No vanilla port binding available");}));
        if(ChatUtil.errors!=1)throw new AssertionError("Missing input must report binding failure");
    }
}
'''

REFLECTION_HARNESS = r'''
import org.xiaoxian.easylan.neoforge.version.ReflectionVersionBridgeSupport;
public class ReflectionHarness extends ReflectionVersionBridgeSupport {
    protected String[] maxPlayerFieldNames(){return new String[]{"maxPlayers","f_11193_"};}
    public static class Players { private int maxPlayers=8; public int getMaxPlayers(){return maxPlayers;} }
    public static class Server {
        final Players players=new Players();int port=25599;
        public Players getPlayerList(){return players;} public int getMaxPlayers(){return players.getMaxPlayers();}
        public int getPort(){return port;}
    }
    public static class SrgPlayers { private int f_11193_=8; public int m_11310_(){return f_11193_;} }
    public static class SrgServer {
        final SrgPlayers list=new SrgPlayers();public SrgPlayers m_6846_(){return list;}
        public int m_7418_(){return list.m_11310_();} public int m_7010_(){return 25599;}
    }
    public static void main(String[] args) {
        var bridge=new ReflectionHarness();var server=new Server();
        if(!bridge.setMaxPlayers(server,99)||server.getMaxPlayers()!=99||server.players.getMaxPlayers()!=99)
            throw new AssertionError("Older Minecraft uses shared effective player-list field");
        org.xiaoxian.EasyLAN.getRuntimeState().port="37563";
        if(!"25599".equals(bridge.resolveLanPort(server)))throw new AssertionError("Cached port is not authoritative");
        server.port=-1;
        if(bridge.resolveLanPort(server)!=null)throw new AssertionError("No stale port after unpublish");
        var srg=new SrgServer();
        if(!bridge.setMaxPlayers(srg,37)||bridge.resolveMaxPlayers(srg)!=37||srg.m_7418_()!=37)
            throw new AssertionError("SRG maximum mapping must be effective");
        if(!"25599".equals(bridge.resolveLanPort(srg)))throw new AssertionError("SRG published-port mapping");
    }
}
'''


class OlderEffectiveSettings(unittest.TestCase):
    def test_inherited_vanilla_input_and_negative_control(self):
        for path in GUI_FILES:
            with self.subTest(path=str(path.relative_to(ROOT))):
                source=path.read_text()
                production=method(source,'private void startLan(Button originalButton)').replace('org.xiaoxian.util.ChatUtil','ChatUtil')
                harness=INHERITED_HARNESS.replace('SYNC_METHOD',method(source,'private void syncTextState()'))
                result=run_java({'InheritedHarness.java':harness.replace('PRODUCTION_METHOD',production)},'InheritedHarness')
                self.assertEqual(result.returncode,0,result.stdout+result.stderr)
                broken=production.replace('vanillaPortTextBox.setValue(PortText);','')
                self.assertNotEqual(broken,production)
                result=run_java({'InheritedHarness.java':harness.replace('PRODUCTION_METHOD',broken)},'InheritedHarness')
                self.assertNotEqual(result.returncode,0,'Negative control did not catch ignored vanilla port field')
                self.assertIn('vanillaPortTextBox = targetEditBox;',source)
                self.assertIn('startLan(finalOriginalButton);',source)

    def test_1215_direct_publication(self):
        path=ROOT/'versions/1.21.5/project/src/main/java/org/xiaoxian/gui/GuiShareToLanEdit.java'
        production=method(path.read_text(),'private void startLan()').replace('org.xiaoxian.EasyLAN','EasyLAN')
        result=run_java({'StartHarness.java':START_HARNESS.replace('PRODUCTION_METHOD',production)},'StartHarness')
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)

    def test_mojang_and_srg_effective_getters(self):
        files={'org/xiaoxian/EasyLAN.java':BRIDGE_FILES['org/xiaoxian/EasyLAN.java'],
               'ReflectionHarness.java':REFLECTION_HARNESS}
        for name in ('ReflectionVersionBridgeSupport','VersionBridge'):
            rel=f'org/xiaoxian/easylan/neoforge/version/{name}.java'
            files[rel]=(GROUP/rel).read_text()
        result=run_java(files,'ReflectionHarness')
        self.assertEqual(result.returncode,0,result.stdout+result.stderr)

    def test_all_lan_handlers_avoid_extra_listener_and_input_echo(self):
        for path in ROOT.glob('**/ShareToLan.java'):
            source=path.read_text()
            with self.subTest(path=str(path.relative_to(ROOT))):
                self.assertNotIn('openLanEndpoint',source)
                self.assertNotIn('startLanPort',source)
                self.assertNotIn('GuiShareToLanEdit.PortText',method(source,'private void updateApiInfo('))
                self.assertIn('server.getPort()',source)
                self.assertIn('server.getMaxPlayers()',source)
                self.assertIn('server.getPlayerList().getMaxPlayers()',source)


if __name__=='__main__':
    unittest.main()
