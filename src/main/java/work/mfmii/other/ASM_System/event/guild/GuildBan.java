package work.mfmii.other.ASM_System.event.guild;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import org.jetbrains.annotations.NotNull;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.EventManager;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GuildBan extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public GuildBan(@Nonnull Class event){
        super(event);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof GuildBanEvent){
            GuildBanEvent event = (GuildBanEvent) genericEvent;
            logger.info(event.getUser()+" banned from "+event.getGuild()+"!");
            JSONArray asms = new Config(Config.ConfigType.JSON).getJSONArray("servers.asm");
            List<String> asm_ids = new ArrayList<>();
            asms.forEach(o -> {
                JSONObject _j1 = new JSONObject(o.toString());
                if(_j1.has("id")){
                    asm_ids.add(_j1.getString("id"));
                }
            });
            User target = event.getUser();
            if (asm_ids.contains(event.getGuild().getId())){
                AtomicReference<String> reason = new AtomicReference<>("UNKNOWN");
                if (event.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)){
                    event.getGuild().retrieveAuditLogs().queueAfter(0, TimeUnit.SECONDS, logs -> {
                        for (AuditLogEntry log : logs) {
                            LocalDateTime now = OffsetDateTime.now(log.getTimeCreated().getOffset()).toLocalDateTime();
                            if (log.getType().equals(ActionType.BAN) && log.getTimeCreated().toLocalDateTime().isAfter(now.minusSeconds(10)) && event.getUser().getId().equals(log.getTargetId())){
                                logger.info("User Banned from ASM server. ID: "+log.getUser().getId()+", Reason: "+log.getReason());
                                reason.set(log.getReason());
                                break;
                            }
                        }
                        banFromASM(event, reason.get(), target);
                    });
                }else {
                    banFromASM(event, reason.get(), target);
                }
                /*
                asm_ids.forEach(s -> {
                    Guild target_guild = event.getJDA().getGuildById(s);
                    if (target_guild == null){
                        logger.warn("Guild written in config.json was not found. ID:"+s);
                    }else {
                        if(!target_guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)){
                            logger.warn("The bot doesn't have the permission to ban at "+target_guild.getName()+"("+s+") ");
                        }else{
                            logger.info("GBAN: "+target+", "+target_guild+", FROM_"+event.getGuild()+", Reason: "+reason.get());
                            target_guild.ban(target, 0, reason.get()).queue();
                        }
                    }
                });

                 */
            }
            return true;
        }else
        return false;
    }

    private void banFromASM(GuildBanEvent event, String reason, User target){
        JSONArray asms = new Config(Config.ConfigType.JSON).getJSONArray("servers.asm");
        List<String> asm_ids = new ArrayList<>();
        asms.forEach(o -> {
            JSONObject _j1 = new JSONObject(o.toString());
            if(_j1.has("id")){
                asm_ids.add(_j1.getString("id"));
            }
        });
        asm_ids.forEach(s -> {
            Guild target_guild = event.getJDA().getGuildById(s);
            if (target_guild == null){
                logger.warn("Guild written in config.json was not found. ID:"+s);
            }else {
                if(!target_guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)){
                    logger.warn("The bot doesn't have the permission to ban at "+target_guild.getName()+"("+s+") ");
                }else if(target_guild.equals(event.getGuild())){
                }else{
                    logger.info("GBAN: "+target+", "+target_guild+", FROM_"+event.getGuild()+", Reason: "+reason);
                    target_guild.ban(target, 0, reason).queue();
                }
            }
        });
    }
}
