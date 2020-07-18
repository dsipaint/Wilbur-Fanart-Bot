package main;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter
{
	private Connection con;
	private Random r;
	
	public CommandListener()
	{
		con = utils.DataHandler.getConnection();
		r = new Random();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] arguments = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		if(arguments[0].equalsIgnoreCase(Main.PREFIX + "fanart"))
		{
			//^fanart [ping/name]
			if(arguments.length == 2 && e.getMessage().getAttachments().size() == 0 && !utils.DataHandler.isImageUrl(arguments[1]))
			{
				try
				{
					String id = null;
					if(e.getMessage().getMentionedUsers().size() > 0 && arguments[1].matches("<@!\\d+>"))
						id = e.getMessage().getMentionedUsers().get(0).getId();
					else
					{
						Member m = utils.Server.getMemberByName(arguments[1]);
						if(m == null)
						{
							e.getChannel().sendMessage("Found no users by " + arguments[1]).queue();
							return;
						}
						
					}
					
					int size = utils.DataHandler.getResultSize("select art from Wilbur_approvedart where discord_id = \"" + id + "\"");
					//i.e. if a user is in the database
					if(size != 0)
					{
						int artno = r.nextInt(size) + 1;
						String quote = utils.DataHandler.getArtByIndex(artno, id);
						
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("Fanart piece #" + artno + " by " + e.getGuild().getMemberById(id).getEffectiveName());
						eb.setImage(quote);
						e.getChannel().sendMessage(eb.build()).queue();
					}
					else
					{
						e.getChannel().sendMessage("No fanart found for " 
								+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
				
			}
			else if(arguments.length >= 3 && arguments[2].matches("\\d+"))
			{
				//^fanart [ping/user] [number]
				
				String id;
				if(e.getMessage().getMentionedUsers().size() > 0 && arguments[1].matches("<@!\\d+>"))
					id = e.getMessage().getMentionedUsers().get(0).getId();
				else
				{
					Member m = utils.Server.getMemberByName(arguments[1]);
					if(m == null)
					{
						e.getChannel().sendMessage("Found no users by " + arguments[1]).queue();
						return;
					}
					id = m.getId();
				}
				
				try
				{
					int size = utils.DataHandler.getResultSize("select art from Wilbur_approvedart where discord_id = \"" + id + "\"");
					if(size != 0)
					{
						if(!(Integer.parseInt(arguments[2]) > 0 && Integer.parseInt(arguments[2]) <= size))
						{
							e.getChannel().sendMessage("Number out of range!").queue();
							return;
						}
						
						String quote = utils.DataHandler.getArtByIndex(Integer.parseInt(arguments[2]), id);
						EmbedBuilder eb = new EmbedBuilder();
						eb.setTitle("Fanart piece #" + arguments[2] + " by " + e.getGuild().getMemberById(id).getEffectiveName());
						eb.setImage(quote);
						e.getChannel().sendMessage(eb.build()).queue();
					}
					else
					{
						e.getChannel().sendMessage("No fanart found for " 
								+ e.getGuild().getMemberById(id).getEffectiveName() + "!").queue();
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
			}
			else
			{	
				//^fanart [url]		
				String id = e.getAuthor().getId();
				
				for(int i = 1; i < arguments.length; i++)
				{
					//img url regex
					if(utils.DataHandler.isImageUrl(arguments[i]))
					{
						try
						{
							Statement s = con.createStatement();
							if(utils.Server.isStaff(e.getMember()))
							{
								s.executeUpdate("insert into Wilbur_approvedart (discord_id, art) values (\""
										+ id + "\", \"" + arguments[i] + "\")");
								
								EmbedBuilder artPost = new EmbedBuilder();
								artPost.setTitle("New Fanart!");
								artPost.setDescription("Check out this Fanart by "
										+ e.getGuild().getMemberById(id).getEffectiveName() + "!");
								artPost.setImage(arguments[i]);
								Main.jda.getTextChannelById("627947374169423882").sendMessage(artPost.build()).queue();
							}
							else
							{
								s.executeUpdate("insert into Wilbur_pendingart (discord_id, art) values (\""
										+ id + "\", \"" + arguments[i] + "\")");
								
								utils.Server.sendLog(userID, arguments[i]);
								e.getChannel().sendMessage("Your fanart has been submitted!").queue();
							}	
						}
						catch(SQLException e1)
						{
							e1.printStackTrace();
						}
					}
					else
						e.getChannel().sendMessage("Invalid image!").queue();
				}
					
					
					//attached images
					if(e.getMessage().getAttachments().size() > 0)
					{
						for(Attachment a : e.getMessage().getAttachments())
						{
							if(a.isImage())
							{
								try
								{
									String imgurl = a.getUrl();
									Statement s = con.createStatement();
									if(utils.Server.isStaff(e.getMember()))
									{
										s.executeUpdate("insert into Wilbur_approvedart (discord_id, art) values (\""
												+ id + "\", \"" + imgurl + "\")");
										
										EmbedBuilder artPost = new EmbedBuilder();
										artPost.setTitle("New Fanart!");
										artPost.setDescription("Check out this fanart by "
												+ e.getGuild().getMemberById(id).getEffectiveName() + "!");
										artPost.setImage(imgurl);
										Main.jda.getTextChannelById("627947374169423882").sendMessage(artPost.build()).queue();
										
									}
									else
									{
										s.executeUpdate("insert into Wilbur_pendingart (discord_id, art) values (\""
												+ id + "\", \"" + imgurl + "\")");
										
										utils.Server.sendLog(userID, imgurl);
										e.getChannel().sendMessage("Your fanart has been submitted!").queue();
									}	
								}
								catch(SQLException e1)
								{
									e1.printStackTrace();
								}
							}
							else
								e.getChannel().sendMessage("Invalid image!").queue();
						}
					}
				}
			
			return;
		}
		
		//^fanarthelp
		if(msg.equalsIgnoreCase(Main.PREFIX + "fanarthelp"))
		{
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("**Fanart Help:**");
			eb.setDescription("**" + Main.PREFIX + "fanart [image/image url]: **"
					+ "submits fanart made by the person using the command- images can be attached instead of using "
					+ "image urls, and you can submit multiple images at a time by attaching"
					+ "more images or adding more image urls to the end of the command");
			eb.appendDescription("\n\n**" + Main.PREFIX + "fanart [ping someone or type the first part of a person's name]: **"
					+ "get a random piece of submitted fanart from the specified person");
			eb.appendDescription("\n\n**" + Main.PREFIX + "fanart [ping someone or type the first part of a person's name] [number]: **"
					+ "get a specific piece of fanart from the specified person");
			eb.appendDescription("\n\n**" + Main.PREFIX + "fanarthelp: **"
					+ "displays this message");
			eb.appendDescription("\n\n**" + Main.PREFIX + "disablefanart: **"
					+ "(staff only) disables the fanart feature");
			e.getChannel().sendMessage(eb.build()).queue();
			
			return;
		}
		
		if(msg.equalsIgnoreCase(Main.PREFIX + "disablefanart") && utils.Server.isStaff(e.getMember()))
		{
			e.getChannel().sendMessage("*Fanart feature disabled-- ask al if you want it back up*").queue();
			Main.jda.shutdownNow();
			System.exit(0);
		}
	}
}
