package bot.moondev;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.EnumSet;

public class TicketCategoryMenu extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equalsIgnoreCase("menu:select")) return;

        String selectedOption = event.getValues().get(0);
        User user = event.getMember().getUser();
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("Guild no encontrado.").setEphemeral(true).queue();
            return;
        }

        // Verificar si el usuario ya tiene un ticket abierto
        if (hasOpenTicket(guild, user)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(new Color(236, 69, 31))
                    .setDescription("❌ Alcanzaste el número máximo de tickets, finalizalos para crear otro.").build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        String parent;

        if (selectedOption.equalsIgnoreCase("Servicios")) {
            parent = "1323803948594892860";
        } else if (selectedOption.equalsIgnoreCase("Ayuda")) {
            parent = "1323803962998001754";
        } else {
            System.out.println("[🌑] No se encontró la categoría para el ticket.");
            return;
        }

        Category category = guild.getCategoryById(parent);

        guild.createTextChannel(selectedOption + "-" + user.getName()).setParent(category)
                .queue(channel -> {

                    channel.getManager()
                            .putPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .putPermissionOverride(guild.getSelfMember(), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .putPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue();

                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Ticket | " + selectedOption)
                            .setDescription("""
                                    > **¡Hola! ¿En que te podemos ayudar?**
                                    > Por favor describenos tu situación y con gusto te ayudaremos lo mas pronto posible, sé paciente.
                                    """)
                            .setColor(0xbca2fd)
                            .build();

                    channel.sendMessage("**[+]**" + member.getUser().getAsMention())
                            .addEmbeds(embed)
                            .setActionRow(
                                    Button.danger("close-ticket", "Cerrar Ticket").withEmoji(Emoji.fromFormatted("🔒"))
                            )
                            .queue();

                    Bot.ticketOwners.put(channel.getId(), member.getId());
                    System.out.println("Se agregó el user id al hashmap");

                    MessageEmbed embed2 = new EmbedBuilder()
                            .setDescription("<:check:1293353179718746240> Tu ticket ha sido creado. (" + channel.getAsMention()+ ")")
                            .setColor(0x08FBA2).build();

                    event.replyEmbeds(embed2).setEphemeral(true).queue();
                });
    }

    private boolean hasOpenTicket(Guild guild, User user) {
        // Verifica los nombres de los canales
        for (TextChannel channel : guild.getTextChannels()) {

            if (channel.getName().contains("-" + user.getName())) {
                return true;
            }
        }
        return false; // No se encontró un ticket abierto para el usuario
    }
}
