package utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Server
{
	public static final String approvalEmoteStr = "white_check_mark", rejectEmoteStr = "x";
	public static final String approvalEmoteUnicode = "\u2705", rejectEmoteUnicode = "\u274C";
	public static final String log_channel_id = "627947245484113920";
	
	public static boolean isStaff(Member m)
	{	
		for(Role r : m.getRoles())
		{
			//admin: 602889336748507164  mod: 565626094917648386
			if(r.getId().equals("602889336748507164") || r.getId().equals("565626094917648386"))
				return true;
		}
		return false;
	}
	
	
	public static void sendLog(String userID, String url)
	{
		/*
		 * This method sends a log to a channel so that quotes can be approved.
		 * This method assumes the message is a legitimate submission syntax-wise
		 */
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("New Fanart Submission:");
		eb.setAuthor(main.Main.jda.getUserById(userID).getAsTag());
		eb.setDescription(userID + "\n" + url);
		eb.setImage(url);
		
		//send to log channel
		main.Main.jda.getTextChannelById(log_channel_id).sendMessage(eb.build()).queue((log) ->
		{
			//emote: white_check_mark
			log.addReaction(approvalEmoteUnicode).queue();
			//emote: x
			log.addReaction(rejectEmoteUnicode).queue();
		});
	}
	
	public static Member getMemberByName(String name)
	{
		Guild guild = main.Main.jda.getGuildById("565623426501443584");
		for(Member m : guild.getMembers())
		{
			if(m.getUser().isBot())
				continue;
			if(m.getNickname() != null && m.getNickname().startsWith(name))
				return m;
			if(m.getUser().getName().startsWith(name))
				return m;
		}
		
		return null;
	}
}
