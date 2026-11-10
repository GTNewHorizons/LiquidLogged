package com.gtnewhorizons.waterlogged.internal.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.gtnewhorizons.waterlogged.api.WaterloggedAPI;

/** Operator command to inspect and change stored fluids: {@code /waterlogged get|set|clear x y z [fluid]}. */
public class WaterloggedCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "waterlogged";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/waterlogged <get|set|clear> <x> <y> <z> [fluid]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 4) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        ChunkCoordinates here = sender.getPlayerCoordinates();
        int x = MathHelper.floor_double(func_110666_a(sender, here.posX, args[1]));
        int y = MathHelper.floor_double(func_110666_a(sender, here.posY, args[2]));
        int z = MathHelper.floor_double(func_110666_a(sender, here.posZ, args[3]));
        World world = sender.getEntityWorld();
        switch (args[0]) {
            case "get": {
                Fluid fluid = WaterloggedAPI.getFluid(world, x, y, z);
                reply(sender, fluid == null ? "none" : fluid.getName());
                break;
            }
            case "set": {
                if (args.length < 5) {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                Fluid fluid = FluidRegistry.getFluid(args[4]);
                if (fluid == null) {
                    reply(sender, "unknown fluid " + args[4]);
                    return;
                }
                reply(sender, WaterloggedAPI.setFluid(world, x, y, z, fluid) ? "set" : "not allowed");
                break;
            }
            case "clear":
                reply(sender, WaterloggedAPI.setFluid(world, x, y, z, null) ? "cleared" : "failed");
                break;
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    static void reply(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }
}
