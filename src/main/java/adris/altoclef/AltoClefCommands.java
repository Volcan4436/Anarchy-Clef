package adris.altoclef;

import adris.altoclef.commands.*;
import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    //todo:
    // Add:
    //  - Bind Command
    //  - Config Command
    //  - Damage Command e.g "<prefix>Damage <amount> <source_optional> <delay_optional>"
    //  - Eat Command
    //  - EditAttributes Command (Lets you Edit any Attribute of your Player through command) e.g: "<prefix>EditAttributes <Attribute> <Value>"
    //  - Ping Command (Check any players ping)
    //  - TPS Command (current TPS)
    //  - Server Command (Get Server Info including Brand, Version, Plugins etc.)
    //  - StealPlugins Command (Finds Server Plugins using Magic Packets)
    //  - SendPacket Command (Send any Packet of your choice at any amount at once)
    //  - PausePackets Command (Pauses all Selected Packets being Sent + Option to "charge them them")
    //  - PacketSelect Command (Select the packets you want to add to the PausePackets Command)
    //  - ResumePackets Command (Resumes all Selected Packets being Sent)
    //  - PacketCharge (Charge a Specific Packet) e.g: "<prefix>PacketCharge <PacketID> <Amount> <ReleaseDelay_optional>"

    //todo:
    // Charge Explained:
    // Packet Charging is where you store packets over time,
    // then sends them all at once which sometimes allows more actions at once,
    // because the server knows they could have happened as such they are accepted.

    public AltoClefCommands() throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new HelpCommand(),
                new GetCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new GotoCommand(),
                new IdleCommand(),
                new CoordsCommand(),
                new StatusCommand(),
                new InventoryCommand(),
                new LocateStructureCommand(),
                new StopCommand(),
                new TestCommand(),
                new FoodCommand(),
                new MeatCommand(),
                new OpenFolderCommand(),
                new ReloadSettingsCommand(),
                new GamerCommand(),
                new MarvionCommand(),
                new PunkCommand(),
                new HeroCommand(),
                new SetGammaCommand(),
                new ListCommand(),
                new CoverWithSandCommand(),
                new CoverWithBlocksCommand(),
                new SelfCareCommand(),
                new CheatMenuCommand(),
                new AnarchyCommand(),
                new BindCommand()
                //new TestMoveInventoryCommand(),
                //    new TestSwapInventoryCommand()
        );
    }
}
