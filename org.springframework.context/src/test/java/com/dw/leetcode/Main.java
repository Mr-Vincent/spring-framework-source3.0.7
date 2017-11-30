package com.dw.leetcode;
/*
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　 ┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　  ┃
 * 　　┃　　　　　　 ┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛Code is far away from bug with the animal protecting
 * 　　　　┃　　　┃    神兽保佑,代码无bug
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * <p>
 * ━━━━━━感觉萌萌哒━━━━━━
 */

/**
 * @author dongw [dongwei2016@gmail.com]
 * @date 2017/11/30
 * @description
 */
public class Main {

	private static class ListNode {
		int val;
		ListNode next;
		ListNode(int x) {
			val = x;
		}
	}

	public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
		while (l1 != null) {
			System.out.println(l1.val);
			l1 = l1.next;
		}
		return new ListNode(1);
    }

	public static void main(String[] args) {
		ListNode head = null;
		for (int i=0;i<10;i++) {
			ListNode listNode = new ListNode(i);
			listNode.next = head;
			head = listNode;
		}

		mergeTwoLists(head,head);


	}
}
