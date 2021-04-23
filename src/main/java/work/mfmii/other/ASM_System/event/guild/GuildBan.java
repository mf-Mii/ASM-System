package work.mfmii.other.ASM_System.event.guild;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import org.jetbrains.annotations.NotNull;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.EventManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuildBan extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public GuildBan(@Nonnull Class event){
        super(event);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof GuildBanEvent){
            GuildBanEvent event = (GuildBanEvent) genericEvent;
            JSONArray asms = new Config(Config.ConfigType.JSON).getJSONArray("servers.asm");
            List<String> asm_ids = new ArrayList<>();
            asms.forEach(o -> {
                JSONObject _j1 = new JSONObject(o.toString());
                if(_j1.has("id")){
                    asm_ids.add(_j1.getString("id"));
                }
            });
            if (asm_ids.contains(event.getGuild().getId())){
                String reason = "UNKNOWN";
                if (event.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)){
                    reason = event.getGuild().retrieveAuditLogs().getLast().getReason();
                }
                String finalReason = reason;
                asm_ids.forEach(s -> {
                    Guild target_guild = event.getJDA().getGuildById(s);
                    if (target_guild == null){
                        System.out.println("Warn: Guild written in config.json was not found. ID:"+s);
                    }else {
                        if(!target_guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)){
                            System.out.println("Warn: The bot doesn't have the permission to ban at "+target_guild.getName()+"("+s+") ");
                        }else{
                            target_guild.ban(event.getUser(), 0, finalReason).queue();
                        }
                    }
                });
            }
            return true;
        }else
        return false;
    }
}
