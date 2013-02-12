package spikes.lucass.sliceWars.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import spikes.lucass.sliceWars.src.AttackOutcome;
import spikes.lucass.sliceWars.src.Cell;
import spikes.lucass.sliceWars.src.CellAttack;
import spikes.lucass.sliceWars.src.DiceThrowOutcome;
import spikes.lucass.sliceWars.src.Player;



public class CellAttackTest {

	@Test
	public void cellAttack_AttackWins(){
		Cell attacker = new Cell();
		Cell defender = new Cell();
		attacker.diceCount = 4;
		Player playerAttacking = Player.Player1;
		attacker.owner = playerAttacking;
		defender.diceCount = 3;
		Player playerDefending = Player.Player2;
		defender.owner = playerDefending;
		int attackDiceResultForAllDice = 3;
		int defenseDiceResultForAllDice = 1;
		DiceThrowerMock diceThrowerMock = new DiceThrowerMock(attackDiceResultForAllDice,defenseDiceResultForAllDice);
		CellAttack cellAttack = new CellAttack(diceThrowerMock);
		AttackOutcome attackOutcome = cellAttack.doAttack(attacker,defender);
		DiceThrowOutcome diceThrowOutcome = attackOutcome.diceThrowOutcome;
		assertDiceThrowResults(diceThrowOutcome);
		Cell newAttackCell = attackOutcome.attackCellAfterAttack;
		assertEquals(newAttackCell.diceCount, 1);
		assertEquals(newAttackCell.owner, playerAttacking);
		Cell newDefenseCell = attackOutcome.attackCellAfterDefense;
		assertEquals(newDefenseCell.diceCount, 3);
		assertEquals(newDefenseCell.owner, playerAttacking);		
	}

	@Test
	public void cellAttack_AttackLoses(){
		Cell attacker = new Cell();
		Cell defender = new Cell();
		attacker.diceCount = 4;
		Player playerAttacking = Player.Player1;
		attacker.owner = playerAttacking;
		defender.diceCount = 3;
		Player playerDefending = Player.Player2;
		defender.owner = playerDefending;
		int attackDiceResultForAllDice = 1;
		int defenseDiceResultForAllDice = 4;
		DiceThrowerMock diceThrowerMock = new DiceThrowerMock(attackDiceResultForAllDice,defenseDiceResultForAllDice);
		CellAttack cellAttack = new CellAttack(diceThrowerMock);
		AttackOutcome attackOutcome = cellAttack.doAttack(attacker,defender);
		Cell newAttackCell = attackOutcome.attackCellAfterAttack;
		assertEquals(newAttackCell.diceCount, 1);
		assertEquals(newAttackCell.owner, playerAttacking);
		Cell newDefenseCell = attackOutcome.attackCellAfterDefense;
		assertEquals(newDefenseCell.diceCount, 3);
		assertEquals(newDefenseCell.owner, playerDefending);
	}
	
	@Test
	public void cellAttack_draw_AttackLoses(){
		Cell attacker = new Cell();
		Cell defender = new Cell();
		attacker.diceCount = 4;
		Player playerAttacking = Player.Player1;
		attacker.owner = playerAttacking;
		defender.diceCount = 4;
		Player playerDefending = Player.Player2;
		defender.owner = playerDefending;
		int attackDiceResultForAllDice = 1;
		int defenseDiceResultForAllDice = 1;
		DiceThrowerMock diceThrowerMock = new DiceThrowerMock(attackDiceResultForAllDice,defenseDiceResultForAllDice);
		CellAttack cellAttack = new CellAttack(diceThrowerMock);
		AttackOutcome attackOutcome = cellAttack.doAttack(attacker,defender);
		Cell newAttackCell = attackOutcome.attackCellAfterAttack;
		assertEquals(newAttackCell.diceCount, 1);
		assertEquals(newAttackCell.owner, playerAttacking);
		Cell newDefenseCell = attackOutcome.attackCellAfterDefense;
		assertEquals(newDefenseCell.diceCount, 4);
		assertEquals(newDefenseCell.owner, playerDefending);
	}
	
	private void assertDiceThrowResults(DiceThrowOutcome diceThrowOutcome) {
		assertArrayEquals(new int[]{3,3,3,3}, diceThrowOutcome.attackDice);
		assertArrayEquals(new int[]{1,1,1}, diceThrowOutcome.defenseDice);
		assertEquals(diceThrowOutcome.attackSum, 12);
		assertEquals(diceThrowOutcome.defenseSum, 3);
	}
	
}