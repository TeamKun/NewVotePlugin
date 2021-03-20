package net.kunmc.lab.newvote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
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

    public boolean vs = false,vget = false;
    public List<String>  SenderList1 = new ArrayList<>(),ReceiverList1 = new ArrayList<>(),List = new ArrayList<>(),SenderList2 = new ArrayList<>(),ReceiverList2 = new ArrayList<>();

    @Override
    public  boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args){
        //投票時に関する処理
        if(cmd.getName().equals("v")){
            if(args.length==1){
                if(vs) {
                    //オンラインプレイヤーを取得しリストに登録する処理
                    Player p = (Player) sender;
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        List.add(player.getName());
                    });
                    //もしconfig.ymlのListに要素が登録されていた時にその要素をリストにする処理
                    reloadConfig();saveConfig();
                    if(getConfig().getStringList("List").size()!=0){
                        List = getConfig().getStringList("List");
                    }
                    List.stream().distinct();
                    //投票の受付処理
                    if (SenderList1.contains(sender.getName())) {
                        sender.sendMessage(ChatColor.YELLOW + "あなたはすでに投票済みです。");
                    } else if (!List.contains(args[0])) {
                        sender.sendMessage(ChatColor.RED + (args[0] + "はリストに存在しません。"));
                    }else {
                        SenderList1.add(p.getName());
                        ReceiverList1.add(args[0]);
                        sender.sendMessage(ChatColor.GREEN + args[0]+"に投票しました。");

                        ScoreboardManager manager = Bukkit.getScoreboardManager();
                        Scoreboard scoreboard = manager.getNewScoreboard();
                        for(Player player : Bukkit.getOnlinePlayers()){
                            player.setScoreboard(scoreboard);
                        }
                        Objective objective = scoreboard.registerNewObjective("touhyoutab","dummy","");
                        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                        objective.getScore(sender.getName());
                        Score score = objective.getScore(sender.getName());
                        score.setScore(1);
                    }
                }else{
                    sender.sendMessage(ChatColor.RED + "投票は開始されていません。");
                }
            }else{
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
                    });
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    Scoreboard scoreboard = manager.getNewScoreboard();
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.setScoreboard(scoreboard);
                    }
                    Objective objective = scoreboard.registerNewObjective("touhyoutab","dummy","");
                    objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        Score score = objective.getScore(player.getName());
                        score.setScore(0);
                    });
                    vs = true;
                    vget = false;

                }
                //二度目の/vsコマンド実行時に関する処理(投票結果開示)
                else {
                    SenderList2 = SenderList1;
                    SenderList1=new ArrayList<>();
                    ReceiverList2=ReceiverList1;
                    ReceiverList1=new ArrayList<>();
                    //リストから重複要素数をもとにしたMapを生成する
                    Map<String, Integer> map = new HashMap<>();
                    for (String s : ReceiverList2) {
                        Integer i = map.get(s);
                        map.put(s, i == null ? 1 : i + 1);
                    }
                    //生成したMapを数字基準で降順にソートする
                    List<Map.Entry<String, Integer>> list_entries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
                    Collections.sort(list_entries, new Comparator<Map.Entry<String, Integer>>() {
                        public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                            return obj2.getValue().compareTo(obj1.getValue());
                        }
                    });
                    //投票結果の表示処理(chat)
                    sender.sendMessage(ChatColor.GOLD + "投票結果を表示します。");
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(ChatColor.AQUA + "==========投票結果==========");
                    });
                    int n = 1;
                    int count = 1;
                    int num = 1;
                    for (Map.Entry<String, Integer> entry : list_entries) {
                        if (count != 1) {
                            if (num != entry.getValue()) {
                                n = n + 1;
                            }
                        }
                        String string = Integer.toString(n);
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.sendMessage(ChatColor.GREEN + string + "位: " + entry.getKey() + " [" + entry.getValue() + "票]");
                        });
                        num = entry.getValue();
                        count++;
                    }
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(ChatColor.AQUA + "==========投票結果==========");
                    });
                    vs = false;
                    vget = true;
                }
                }else{
                    sender.sendMessage(ChatColor.RED+"このコマンドを実行する権限がありません。必要権限:OP");
                }
            }else{
                sender.sendMessage(ChatColor.RED+"コマンドの形式が異なります。/vs で投票を開始することができます。");
            }
        }
        //投票先開示に関する処理
        else if(cmd.getName().equals("vget"))
            if(args.length==0) {
                if(sender.isOp()) {
                    if (vget) {
                        //投票先の表示処理
                        sender.sendMessage(ChatColor.GOLD + "投票先を開示します。");
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.sendMessage(ChatColor.AQUA + "==========投票先一覧==========");
                            player.sendMessage(ChatColor.GREEN+ "    投票者            投票先  ");
                        });
                        for (int i = 0; i < SenderList2.size(); i++) {
                            int num = i;
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                player.sendMessage(ChatColor.GREEN + SenderList2.get(num) +ChatColor.WHITE + "   ➡   " +ChatColor.GREEN + ReceiverList2.get(num));
                            });
                        }
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.sendMessage(ChatColor.AQUA + "==========投票先一覧==========");
                        });
                        vs = false;
                        vget = false;
                    }else{
                        sender.sendMessage(ChatColor.RED + "投票が完了していません。");
                    }
                }else{
                    sender.sendMessage(ChatColor.RED+"このコマンドを実行する権限がありません。必要権限:OP");
                }
            }else{
                sender.sendMessage(ChatColor.RED+"コマンドの形式が異なります。/vget で投票先を手得できます。");
            }
        return true;
    }

    //Tab補完
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String Label, String[] args) {
        if (args.length == 1) {
            if (cmd.getName().equals("v")) {
                reloadConfig();saveConfig();
                if (getConfig().getStringList("List").size() != 0) {
                    return getConfig().getStringList("List");
                } else {
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
