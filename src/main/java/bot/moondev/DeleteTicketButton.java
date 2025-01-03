package bot.moondev;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeleteTicketButton extends ListenerAdapter {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!event.getButton().getId().equalsIgnoreCase("delete-ticket")) return;

        Guild guild = event.getGuild();
        Member member = event.getMember();
        assert guild != null;

        if (!member.getPermissions().contains(Permission.ADMINISTRATOR) || !member.isOwner()) {
            event.reply("No tienes permitida esa acción.").setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = new EmbedBuilder()
                .setDescription("El ticket será eliminado en 5 segundos...")
                .build();


        event.reply("El ticket será eliminado en 5 segundos...")
                .delay(1, TimeUnit.SECONDS)
                .flatMap(it -> it.editOriginal("El ticket será eliminado en 4 segundos..."))
                .delay(1, TimeUnit.SECONDS)
                .flatMap(it -> it.editMessage("El ticket será eliminado en 3 segundos..."))
                .delay(1, TimeUnit.SECONDS)
                .flatMap(it -> it.editMessage("El ticket será eliminado en 2 segundos..."))
                .delay(1, TimeUnit.SECONDS)
                .flatMap(it -> it.editMessage("El ticket será eliminado en 1 segundo..."))
                .queue();


        event.getChannel().getIterableHistory().queue(messages -> {
            try {
                // Invertir la lista de mensajes para que los más antiguos aparezcan primero
                List<Message> reversedMessages = messages.stream().sorted((m1, m2) -> m1.getTimeCreated().compareTo(m2.getTimeCreated())).toList();

                // Crear el archivo del transcript
                File transcriptFile = generarTranscript(reversedMessages, event.getChannel().getName());

                String ticketCreationTime = reversedMessages.get(reversedMessages.size() - 1).getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                String channelName = event.getChannel().getName();
                String memberName = channelName.split("-")[1];

                Member memberLog = event.getGuild().getMembers().stream()
                        .filter(member1 -> member1.getEffectiveName().equalsIgnoreCase(memberName))
                        .findFirst().orElse(null);


                // Crear embed con información del ticket
                MessageEmbed transcriptEmbed = new EmbedBuilder()
                        .setTitle("Información del Ticket")
                        .setDescription(String.format("""
                                   Ticket: #%s
                                   Creador: %s
                                   Creado el: %s
                                   """, channelName, memberName, ticketCreationTime))
                        .setColor(0xbca2fd)
                        .build();

                // Enviar el archivo del transcript al canal de logs con el embed
                TextChannel logChannel = event.getGuild().getTextChannelById("1323772335781576805");

                if (logChannel != null) {
                    logChannel.sendMessageEmbeds(transcriptEmbed).addFiles(FileUpload.fromData(transcriptFile)).queue();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        });

        // Programar la eliminación del canal después de 5 segundos
        scheduler.schedule(() -> event.getChannel().asTextChannel().delete().queue(), 6, TimeUnit.SECONDS);
    }

    private File generarTranscript(List<Message> messages, String channelName) throws IOException {
        File transcriptFile = new File("transcript_" + channelName + ".html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(transcriptFile))) {
            writer.write("<html><head><title>Transcript</title><style>");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; color: #DCDFE4; background-color: #36393F; }");
            writer.write("h1 { color: #FFFFFF; }");
            writer.write("div.message { border: 1px solid #4F545C; padding: 10px; margin-bottom: 10px; border-radius: 5px; background-color: #2F3136; display: flex; align-items: flex-start; }");
            writer.write("img.avatar { border-radius: 50%; width: 32px; height: 32px; margin-right: 10px; }");
            writer.write("div.embed { border: 1px solid #7289DA; padding: 10px; border-radius: 5px; margin-bottom: 10px; background-color: #2F3136; }");
            writer.write("h2.embed-title { color: #7289DA; margin: 0; }");
            writer.write("p { margin: 5px 0; }");
            writer.write("p.embed-footer { font-style: italic; color: #99AAB5; }");
            writer.write("</style></head><body>");
            writer.write("<h1>Transcript for #" + channelName + "</h1>");

            for (Message message : messages) {
                String authorAvatarUrl = message.getAuthor().getEffectiveAvatarUrl(); // Obtener URL del avatar del autor

                writer.write("<div class='message'>");
                writer.write("<img class='avatar' src='" + authorAvatarUrl + "' alt='Avatar'>");
                writer.write("<div>");
                writer.write("<strong>" + message.getAuthor().getName() + "</strong> <em>" + message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</em>");
                writer.write("<br/>");

                // Contenido del mensaje
                String content = message.getContentRaw();

                // Reemplazar menciones de usuarios
                for (User user : message.getMentions().getUsers()) {
                    content = content.replace("<@" + user.getId() + ">", "@" + user.getName());
                }
                // Reemplazar menciones de roles
                for (Role role : message.getMentions().getRoles()) {
                    content = content.replace("<@&" + role.getId() + ">", "@" + role.getName());
                }
                // Escribir el contenido modificado
                writer.write("<p>" + content.replaceAll("\n", "<br/>") + "</p>");

                // Agregar embeds
                for (MessageEmbed embed : message.getEmbeds()) {
                    writer.write("<div class='embed' style='border-color: " + (embed.getColorRaw() != 0 ? "#" + Integer.toHexString(embed.getColorRaw()).toUpperCase() : "#7289DA") + ";'>");
                    if (embed.getTitle() != null) {
                        writer.write("<h2 class='embed-title'>" + embed.getTitle() + "</h2>");
                    }
                    if (embed.getDescription() != null) {
                        // Reemplazar menciones en la descripción del embed
                        String description = embed.getDescription();
                        for (User user : message.getMentions().getUsers()) {
                            description = description.replace("<@" + user.getId() + ">", "@" + user.getName());
                        }
                        for (Role role : message.getMentions().getRoles()) {
                            description = description.replace("<@&" + role.getId() + ">", "@" + role.getName());
                        }
                        writer.write("<p>" + description.replaceAll("\n", "<br>") + "</p>");
                    }
                    for (MessageEmbed.Field field : embed.getFields()) {
                        // Reemplazar menciones en los campos del embed
                        String fieldValue = field.getValue();
                        for (User user : message.getMentions().getUsers()) {
                            assert fieldValue != null;
                            fieldValue = fieldValue.replace("<@" + user.getId() + ">", "@" + user.getName());
                        }
                        for (Role role : message.getMentions().getRoles()) {
                            assert fieldValue != null;
                            fieldValue = fieldValue.replace("<@&" + role.getId() + ">", "@" + role.getName());
                        }
                        writer.write("<p><strong>" + field.getName() + ":</strong> " + fieldValue + "</p>");
                    }
                    if (embed.getFooter() != null) {
                        writer.write("<p class='embed-footer'>" + embed.getFooter().getText() + "</p>");
                    }
                    writer.write("</div>");
                }

                writer.write("</div>");
                writer.write("</div>");
            }

            writer.write("</body></html>");
        }
        return transcriptFile;
    }
}

