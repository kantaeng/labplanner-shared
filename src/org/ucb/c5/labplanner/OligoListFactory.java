package org.ucb.c5.labplanner;

import java.util.ArrayList;
import java.util.List;
import org.ucb.c5.constructionfile.model.ConstructionFile;
import org.ucb.c5.constructionfile.model.Oligo;
import org.ucb.c5.constructionfile.model.Operation;
import org.ucb.c5.constructionfile.model.PCR;
import org.ucb.c5.constructionfile.model.Polynucleotide;
import org.ucb.c5.constructionfile.model.Step;

/**
 *
 * @author J. Christopher Anderson
 */
public class OligoListFactory {

    public void initiate() {
    }

    public List<Oligo> run(List<ConstructionFile> cfList) {
        List<Oligo> oligoList = new ArrayList<>();
        
        //Create samples for ever sample-generating step in every construction file
        for (ConstructionFile cf : cfList) {
            String cfPdt = cf.getPdtName();
            for (Step astep : cf.getSteps()) {
                if(astep.getOperation() != Operation.pcr) {
                    continue;
                }
                
                //Add the forward primer
                PCR pcrStep = (PCR) astep;
                String oligo1Name = pcrStep.getOligo1();
                String oligo1Seq = cf.getSequences().get(oligo1Name).getSequence();
                Oligo oligo1 = new Oligo(oligo1Name, oligo1Seq, "Forward oligo in construction of " + cfPdt);
                oligoList.add(oligo1);
                
                //Add the reverse primer
                String oligo2Name = pcrStep.getOligo2();
                String oligo2Seq = cf.getSequences().get(oligo2Name).getSequence();
                Oligo oligo2 = new Oligo(oligo2Name, oligo2Seq, "Reverse oligo in construction of " + cfPdt);
                oligoList.add(oligo2);
                
                //Add any oligo templates (i.e. for PCA)
                for(String tempName : pcrStep.getTemplates()) {
                    Polynucleotide tempPoly = cf.getSequences().get(tempName);
                    if(tempPoly.isIsDoubleStranded()) {
                        continue;
                    }
                    String tempSeq = tempPoly.getSequence();
                    Oligo tempOligo = new Oligo(tempName, tempSeq, "Template oligo in construction of " + cfPdt);
                    oligoList.add(tempOligo);
                }
            }
        }
        return oligoList;
    }
}
