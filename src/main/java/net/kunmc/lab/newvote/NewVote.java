package net.kunmc.lab.newvote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.stream.Collectors;

public final class NewVote extends JavaPlugin implements CommandExecutor, TabCompleter {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveConfig();
        this.getCommand("v").setExecutor(this);
        this.getCommand("v").setTabCompleter(this);
        this.getCommand("vs").setExecutor(this);
        this.getCommand("vget").setExecutor(this);
        getServer().getLogger().info(ChatColor.AQUA+"NewVotePlugin by Yanaaaaa");
    }

    //vs=投票時判定用,vget=投票開示時判定用
    static boolean vs = false,vget = false;
    //SenderList=投票者のリスト,ReceiverList=被投票者のリスト
    static List<String> SenderList1 = new ArrayList<>(),ReceiverList1 = new ArrayList<>();
    static List<String> SenderList2 = new ArrayList<>(),ReceiverList2 = new ArrayList<>();
    //List=投票先のリスト
    static List<String> List = new ArrayList<>();

    @Override
    public  boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args){
        //投票時に関する処理
        if(cmd.getName().equals("v")){
            if(args.length==1){
                //投票時の処理
                if(vs) {
                    //オンラインプレイヤーを取得しリストに登録する処理
                    Player p = (Player) sender;
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        List.add(player.getName());
                    });
                    //もしconfig.ymlのListに要素が登録されていた時にその要素を参照する処理
                    reloadConfig();saveConfig();
                    if(getConfig().getStringList("List").size()!=0){
                        List = getConfig().getStringList("List");
                    }
                    List.stream().distinct();

                    //投票の受付処理
                    //例外:投票済みの場合の処理
                    if (SenderList1.contains(sender.getName())) {
                        sender.sendMessage(ChatColor.YELLOW + "あなたはすでに投票済みです。");
                    }
                    //例外:投票先のリストにプレイヤーが存在しない場合
                    else if (!List.contains(args[0])) {
                        sender.sendMessage(ChatColor.RED + (args[0] + "はリストに存在しません。"));
                    }
                    //投票完了の通知
                    else {
                        //投票者、投票先を保存し投票完了を通知する。
                        SenderList1.add(p.getName());
                        ReceiverList1.add(args[0]);
                        ScoreBoardLogic.setVoteStatus(1,p);
                        sender.sendMessage(ChatColor.GREEN + args[0]+"に投票しました。");
                    }
                }
                //例外:投票が開始されていないときの処理
                else{
                    sender.sendMessage(ChatColor.RED + "投票は開始されていません。");
                }
            }
            //例外:コマンドの形式が異なっているときの処理。
            else{
                sender.sendMessage(ChatColor.RED + "コマンドの形式が異なります。/v <投票先のプレイヤー名>で投票してください。");
            }
        }
        //投票開始・投票結果開示に関する処理
        else if(cmd.getName().equals("vs")){
            if(args.length==0) {
                if(sender.isOp()){
                //一度目の/vsコマンド使用時に関する処理(投票開始)
                if (!vs) {
                    reloadConfig();saveConfig();
                    //投票開始の連絡を全プレイヤーに行う
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(ChatColor.GOLD + "投票が開始されました。");
                        player.sendMessage(ChatColor.GREEN + "/v <投票先のプレイヤー名>で投票してください。");
                        ScoreBoardLogic.setVoteStatus(0,player);
                    });
                    //投票開示を有効化する。
                    vs = true;vget = false;

                }
                //二度目の/vsコマンド実行時に関する処理(投票結果開示)
                else {
                    //数値の引き渡し
                    SenderList2 = SenderList1; SenderList1=new ArrayList<>();
                    ReceiverList2=ReceiverList1; ReceiverList1=new ArrayList<>();
                    //投票結果の表示
                    VoteResultLogic.sendVotingResult(ReceiverList2);
                    ScoreBoardLogic.setVoteStatus(2,(Player) sender);
                    //投票開始、投票先開示を有効にする
                    vs = false;vget = true;
                }
                }
                //例外:非OP権限者実行時
                else{
                    sender.sendMessage(ChatColor.RED+"このコマンドを実行する権限がありません。必要権限:OP");
                }
            }
            //例外:コマンドの形式が異なっているときの処理。
            else{
                sender.sendMessage(ChatColor.RED+"コマンドの形式が異なります。/vs で投票を開始することができます。");
            }
        }
        //投票先開示に関する処理
        else if(cmd.getName().equals("vget"))
            if(args.length==0) {
                if(sender.isOp()) {
                    //投票先開示の処理
                    if (vget) {
                        //各投票者の投票先の表示
                        VoteResultLogic.sendVotingDestination(SenderList2,ReceiverList2);
                        //投票開始を有効、投票先開示を無効にする。
                        vs = false;vget = false;
                    }
                    //例外:投票が開始もしくは終了されていないときの処理
                    else{
                        sender.sendMessage(ChatColor.RED + "投票が完了していません。");
                    }
                }
                //非OP権限者実行時
                else{
                    sender.sendMessage(ChatColor.RED+"このコマンドを実行する権限がありません。必要権限:OP");
                }
            }
            //例外:コマンドの形式が異なっているときの処理。
            else{
                sender.sendMessage(ChatColor.RED+"コマンドの形式が異なります。/vget で投票先を手得できます。");
            }
        return true;
    }

    //・/v <投票先>コマンド実行時のTab補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args) {
        if (args.length == 1) {
            if (cmd.getName().equals("v")) {
                reloadConfig();saveConfig();
                //Config参照時のTab補完
                if (getConfig().getStringList("List").size() != 0) {
                    return getConfig().getStringList("List");
                }
                //通常時(オンラインプレイヤー参照時)のTab補完
                else {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(s -> s.startsWith(args[0]))
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }

}
