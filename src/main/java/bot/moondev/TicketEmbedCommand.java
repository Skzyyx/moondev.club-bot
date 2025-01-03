package bot.moondev;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class TicketEmbedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("ticket-embed")) return;

        MessageEmbed embed = new EmbedBuilder()
                .setDescription("""
                        # `Soporte︱moondev.club`
                        
                        > ¡Crea un ticket en la categoría donde necesites información y estaremos contentos de ayudarte!
                        
                        > **Servicios:** Si necesitas cotizar alguna modalidad/plugin/web.
                        > **Ayuda:** Si tienes algún problema con la compra de algún producto.
                        """)
                .setColor(0xbca2fd)
                .build();

        TextChannel channel = event.getChannel().asTextChannel();

        StringSelectMenu menu = StringSelectMenu.create("menu:select")
                .setPlaceholder("Selecciona la categoría de tu ticket")
                .addOption("Servicios", "Servicios", "Cotiza alguna modalidad/plugin/web.", Emoji.fromFormatted("🔧"))
                .addOption("Ayuda", "Ayuda", "Problemas con la compra de algun producto.", Emoji.fromFormatted("📫"))
                .build();

        channel.sendMessageEmbeds(embed).addActionRow(menu).queue();
    }
}
