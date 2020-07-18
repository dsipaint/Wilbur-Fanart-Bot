package main;
import java.sql.Connection;
import java.sql.SQLException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ApprovalListener extends ListenerAdapter
{
	private Connection con;
	
	public ApprovalListener()
	{
		con = utils.DataHandler.getConnection();
	}
	
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e)
	{
		if(e.getChannel().getId().equals(utils.Server.log_channel_id))
		{
			//bot ignores all non-staff
			if(!utils.Server.isStaff(e.getMember()))
				return;
			
			e.getChannel().retrieveMessageById(e.getMessageId()).queue( (msg) ->
			{
				//if a reaction is added to a message in the RIGHT channel but it isn't a reaction to the bot's message
				if(!msg.getAuthor().getId().equals("618543880438153246"))
					return;
				
				//only interested in the right reactions
				if(!e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode) && !e.getReactionEmote().getName().equals(utils.Server.rejectEmoteUnicode))
					return;
				
				//every embed log should have an image with a url, and a user id
				MessageEmbed em = msg.getEmbeds().get(0);
				//formatted like this in utils.Server.sendLog() (may change in the future)
				String userID = em.getDescription().split("\n")[0];
				String imgurl = em.getDescription().split("\n")[1];
				
				try
				{
					//always remove from the pending quotes list
					con.createStatement().executeUpdate("delete from Wilbur_pendingart where art = \""
							+ imgurl + "\"");
					
					//approved
					if(e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode))
					{
						con.createStatement().executeUpdate("insert into Wilbur_approvedart (discord_id, art)"
								+ "values (\"" + userID + "\", \"" + imgurl + "\")");
						
						EmbedBuilder quotePost = new EmbedBuilder();
						quotePost.setTitle("New Fanart!");
						quotePost.setDescription("Check out this fanart by "
								+ e.getGuild().getMemberById(userID).getEffectiveName() + "!");
						quotePost.setImage(imgurl);
						Main.jda.getTextChannelById("627947374169423882").sendMessage(quotePost.build()).queue();
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
				
				msg.delete().queue();
			});
		}
	}
}
