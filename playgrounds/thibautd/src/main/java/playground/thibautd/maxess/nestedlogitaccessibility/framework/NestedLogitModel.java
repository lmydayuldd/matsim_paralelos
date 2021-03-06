/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.maxess.nestedlogitaccessibility.framework;

/**
 * @author thibautd
 */
// making it generic and specifying the nests as enum might look (or be) more complicated than it should be,
// but it allows to make different elements (utility or sampler) for the same nesting structure, and combine them safely.
public class NestedLogitModel<N extends Enum<N>> {
	private final double mu;
	private final Utility<N> utility;
	private final ChoiceSetIdentifier<N> choiceSetIdentifier;

	public NestedLogitModel(
			final Utility<N> utility,
			final ChoiceSetIdentifier<N> choiceSetIdentifier ) {
		this( 1 , utility , choiceSetIdentifier );
	}

	public NestedLogitModel(
			final double mu,
			final Utility<N> utility,
			final ChoiceSetIdentifier<N> choiceSetIdentifier ) {
		this.mu = mu;
		this.utility = utility;
		this.choiceSetIdentifier = choiceSetIdentifier;
	}

	public ChoiceSetIdentifier<N> getChoiceSetIdentifier() {
		return choiceSetIdentifier;
	}

	public double getMu() {
		return mu;
	}

	public Utility<N> getUtility() {
		return utility;
	}
}
