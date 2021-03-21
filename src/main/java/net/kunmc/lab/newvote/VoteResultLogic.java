package net.kunmc.lab.newvote;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

//投票関係の結果生成
public class VoteResultLogic {

    /**
     * 投票結果チャット欄に生成する
     *
     * @param receivers　リスト　投票先のリスト
     */
    static void sendVotingResult(List<String> receivers){
        //リストの重複要素数と被投票者をもとにしたMapを生成する
        Map<String, Integer> map = new HashMap<>();
        for (String s : receivers) {
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

        //投票結果の表示処理(全プレイヤーに表示)
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.GOLD + "投票結果を表示します。");
            player.sendMessage(ChatColor.AQUA + "==========投票結果==========");
        });
        int n = 1;//順位用変数
        int count = 1;//実行回数変数
        int num = 1;//数値比較用変数
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
    }

    /**
     * 各投票者の投票先をチャット欄に生成する。
     *
     * @param senders　リスト 投票者のリスト
     * @param receivers　リスト　投票先のリスト
     */
    static void sendVotingDestination(List<String> senders, List<String> receivers){
        //投票先の表示処理(全プレイヤーに表示)
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.GOLD + "投票先を開示します。");
            player.sendMessage(ChatColor.AQUA + "==========投票先一覧==========");
            player.sendMessage(ChatColor.GREEN+ "  投票者               投票先  ");
        });
        for (int i = 0; i < senders.size(); i++) {
            int num = i;
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(ChatColor.GREEN + senders.get(num) +ChatColor.WHITE + "   ➡   " +ChatColor.GREEN + receivers.get(num));
                ScoreBoardLogic.setVoteResult(senders.get(num),receivers.get(num));
            });
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.AQUA + "==========投票先一覧==========");
        });
    }
}
