/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class Sword extends MeleeWeapon {
	
	{
		image = ItemSpriteSheet.SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 3;
	}

	@Override
	public int abilityChargeUse() {
		return Dungeon.hero.buff(CleaveTracker.class) != null ? 0 : 1;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		Sword.cleaveAbility(hero, target, 1.27f, this);
	}

	public static void cleaveAbility(Hero hero, Integer target, float dmgMulti, MeleeWeapon wep){
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(wep, "ability_no_target"));
			return;
		}

		if (!hero.canAttack(enemy)){
			GLog.w(Messages.get(wep, "ability_bad_position"));
			return;
		}

		hero.sprite.attack(enemy.pos, new Callback() {
			@Override
			public void call() {
				if (hero.attack(enemy, dmgMulti, 0, Char.INFINITE_ACCURACY)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				}
				wep.onAbilityUsed(hero);

				if (!enemy.isAlive()){
					hero.next();
					Buff.prolong(hero, CleaveTracker.class, 4f); //1 less as attack was instant
				} else {
					hero.spendAndNext(hero.attackDelay());
					if (hero.buff(CleaveTracker.class) != null) {
						hero.buff(CleaveTracker.class).detach();
					}
				}
			}
		});
	}

	public static class CleaveTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.DUEL_CLEAVE;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
	}

}
