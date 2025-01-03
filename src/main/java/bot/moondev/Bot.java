package bot.moondev;

import bot.moondev.Buttons.CloseTicketButton;
import bot.moondev.Buttons.DeleteTicketButton;
import bot.moondev.Buttons.OpenTicketButton;
import bot.moondev.Commands.ReviewCommand;
import bot.moondev.EventListeners.MemberJoinListener;
import bot.moondev.TicketSystems.TicketCategoryMenu;
import bot.moondev.TicketSystems.TicketEmbedCommand;
import bot.moondev.Utils.Config;
import com.sun.tools.javac.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Bot {

    public static String guildId;
    public static JDA shardMan;
    public static JDABuilder builder;

    public static Map<String, String> ticketOwners = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        String folderName = "resources";
        String configFileName = "config.yml";

        File folder = new File(folderName);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("[🌑] Carpeta 'sources' creada.");
            } else {
                System.out.println("[🌑] No se pudo crear la carpeta resources.");
                return;
            }
        }

        File configFile = new File(folder, configFileName);
        if (!configFile.exists()) {
            try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    throw new RuntimeException("[🌑] El archivo de configuración no se encuentra en el JAR.");
                }
                Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[🌑] El archivo 'config.yml' ha sido copiado a la carpeta 'resources'.");
            } catch (IOException e) {
                System.err.println("[🌑] No se pudo copiar el archivo de configuración: " + e.getMessage());
                throw new RuntimeException("[🌑] Error al copiar el archivo de configuración.", e);
            }
        } else {
            System.out.println("[🌑] El archivo 'config.yml' ya existe en la carpeta 'resources'.");
        }

        // Ahora puedes cargar el archivo config.yml desde el sistema de archivos
        try (InputStream in = Files.newInputStream(configFile.toPath())) {
            Config.initialize(in);
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("No se pudo cargar el archivo de configuración.");
        }

        guildId = Config.getString("guild_id");
        String token  = Config.getString("bot_token");

        builder = JDABuilder.createDefault(token)
                .addEventListeners(new ReviewCommand())
                .addEventListeners(new TicketEmbedCommand())
                .addEventListeners(new TicketCategoryMenu())
                .addEventListeners(new CloseTicketButton())
                .addEventListeners(new OpenTicketButton())
                .addEventListeners(new DeleteTicketButton())
                .addEventListeners(new MemberJoinListener());

        builder.setStatus(OnlineStatus.IDLE);

        builder.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT);

        builder.setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.customStatus("🌑"));

        shardMan = builder.build();

        shardMan.awaitReady();

        CommandListUpdateAction commands = shardMan.updateCommands();

        commands.addCommands(
                Commands.slash("review", "Envía una reseña sobre un servicio nuestro recibido.")
                        .addOption(OptionType.STRING, "product", "Nombre exacto del producto.", true)
                        .addOption(OptionType.STRING, "message", "Mensaje de opinión sobre el producto adquirido.", true)
                        .addOptions(
                                new OptionData(OptionType.INTEGER, "stars", "Califica la calidad del producto.", true)
                                        .setMinValue(1) // Valor mínimo
                                        .setMaxValue(5) // Valor máximo
                        )
                        .setGuildOnly(true),
                Commands.slash("ticket-embed", "Envia el embed para los tickets.")
                        .setGuildOnly(true)
        ).queue();
    }
}