/* Created on Apr 19, 2020 */
package net.semanlink.arxiv;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.Test;

import net.semanlink.metadataextraction.ExtractorData;
import net.semanlink.semanlink.DataLoader;
import net.semanlink.semanlink.SLDocument;
import net.semanlink.semanlink.SLModel;
/**
 * loop to update the metadata about arxiv bookmarks
 */



public class ArxivLoopUpdateTest {

// @Test viré parce que met à jour les sl.rdf: ennuyeux pour git. TODO
public final void test() throws Exception {
	SLModel m = DataLoader.getSLModel();
	Client client = ClientBuilder.newClient();
	ArxivMetadataExtractor extractor = new ArxivMetadataExtractor();
	Iterator<SLDocument> docs = m.documents();
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		String arxivNum = Arxiv.sldoc2arxivNum(doc, m);
		if (arxivNum == null) continue;
		
		System.out.println(arxivNum + " : " + doc.getURI());
		ExtractorData extractorData = new ExtractorData(doc, m, client);
		
		// it happens that this doesn't handle keywords: very good here
		// (we do  not want to recompute them)
		extractor.doIt(extractorData);
	}
	DataLoader.cleanTestDir();
}

// @Test 
public final void ATTENTION_MODIF_VRAI_SEMANLINK_FPS() throws Exception {
	SLModel m = DataLoader.fpsSLModel();
	Client client = ClientBuilder.newClient();
	ArxivMetadataExtractor extractor = new ArxivMetadataExtractor();
	Iterator<SLDocument> docs = m.documents();
	int k = 0;
	for (;docs.hasNext();) {
		SLDocument doc = docs.next();
		
		String arxivNum = Arxiv.sldoc2arxivNum(doc, m);
		if (arxivNum == null) continue;
		
		System.out.println(arxivNum + " / " + doc.getDate() + " : " + doc.getURI());
		
		try {
			ExtractorData extractorData = new ExtractorData(doc, m, client);
			
			// it happens that this doesn't handle keywords: very good here
			// (we do  not want to recompute them)
			extractor.doIt(extractorData);
		} catch (Exception e) {
			// exception on http://arxiv.org/pdf/cs.DS/0310019
			e.printStackTrace();
		}
		
		k++;
		// if (k > 10) break;
	}
}

// voir physics/0004057 / 2019-08-15 : http://127.0.0.1:8080/semanlink/doc/2019/08/_physics_0004057_the_informati 
// https://arxiv.org/abs/physics/0004057

}

/*
1703.00993 / 2017-08-28 : https://arxiv.org/abs/1703.00993 / 
1909.04120 / 2019-09-18 : http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_04120_span_selection_pre / 
1711.09677 / 2019-02-02 : https://arxiv.org/abs/1711.09677 / 
1811.05370 / 2018-11-20 : https://arxiv.org/abs/1811.05370 / 
1803.11175 / 2018-05-29 : https://arxiv.org/abs/1803.11175 / 
1904.02342 / 2019-08-23 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1904_02342_text_generation_fr / 
1902.09229 / 2019-03-20 : https://arxiv.org/abs/1902.09229 / 
1904.08398 / 2019-04-18 : https://arxiv.org/abs/1904.08398 / 
1906.04341 / 2019-06-21 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1906_04341_what_does_bert_loo / 
1601.07752 / 2016-05-28 : http://arxiv.org/abs/1601.07752 / 
1712.09405 / 2017-12-29 : https://arxiv.org/abs/1712.09405 / 
1906.08237 / 2019-06-21 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1906_08237_xlnet_generalized / 
1709.08568 / 2017-09-29 : https://arxiv.org/abs/1709.08568 / 
1905.05950 / 2019-05-18 : http://127.0.0.1:8080/semanlink/doc/2019/05/_1905_05950_bert_rediscovers_t / 
1905.11852 / 2019-12-05 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1905_11852_educe_explaining_ / 
1907.03950 / 2019-07-10 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_03950_learning_by_abstra / 
1307.5101 / 2018-03-04 : https://arxiv.org/abs/1307.5101 / 
1607.00570 / 2017-06-09 : https://arxiv.org/abs/1607.00570 / 
1603.05106 / 2016-03-18 : http://arxiv.org/abs/1603.05106v1 / 
1703.02507 / 2019-03-25 : https://arxiv.org/abs/1703.02507 / 
1802.07569 / 2020-01-01 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1802_07569_continual_lifelong / 
1907.05242 / 2019-07-13 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_05242_large_memory_layer / 
1911.02168 / 2020-03-22 : http://127.0.0.1:8080/semanlink/doc/2020/03/_1911_02168_coke_contextualiz / 
1411.4166 / 2018-02-25 : https://arxiv.org/abs/1411.4166 / 
1901.03136 / 2019-02-15 : https://arxiv.org/abs/1901.03136 / 
1806.04411 / 2019-04-11 : https://arxiv.org/abs/1806.04411 / 
1602.04938 / 2018-09-09 : https://arxiv.org/abs/1602.04938 / 
1511.08154 / 2016-01-12 : http://arxiv.org/abs/1511.08154 / 
1803.05651 / 2018-03-20 : https://arxiv.org/abs/1803.05651 / 
1902.10197 / 2020-03-03 : http://127.0.0.1:8080/semanlink/doc/2020/03/_1902_10197_rotate_knowledge_ / 
1909.07606 / 2020-03-08 : http://127.0.0.1:8080/semanlink/doc/2020/03/_1909_07606_k_bert_enabling_l / 
1601.01272 / 2016-01-09 : http://arxiv.org/abs/1601.01272 / 
1908.01580 / 2019-08-15 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1908_01580_the_hsic_bottlenec / 
1810.10531 / 2019-06-29 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1810_10531_a_mathematical_the / 
1801.06146 / 2018-01-19 : https://arxiv.org/abs/1801.06146 / 
1810.07150 / 2018-10-22 : https://arxiv.org/abs/1810.07150 / 
1802.01528 / 2020-02-19 : http://127.0.0.1:8080/semanlink/doc/2020/02/_1802_01528_the_matrix_calculu / 
1508.01991 / 2018-03-05 : https://arxiv.org/abs/1508.01991 / 
2003.02320 / 2020-03-07 : http://127.0.0.1:8080/semanlink/doc/2020/03/_2003_02320_knowledge_graphs / 
1601.01343 / 2019-01-27 : https://arxiv.org/abs/1601.01343 / 
1004.5370 / 2017-11-07 : https://arxiv.org/pdf/1004.5370.pdf / 
1706.04902 / 2018-05-20 : https://arxiv.org/abs/1706.04902 / 
1506.01094 / 2015-10-31 : http://arxiv.org/abs/1506.01094 / 
1808.02590 / 2019-08-25 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1808_02590_a_tutorial_on_netw / 
1812.04616 / 2018-12-14 : https://arxiv.org/abs/1812.04616 / 
1603.08861 / 2018-02-13 : https://arxiv.org/abs/1603.08861 / 
1510.00726 / 2017-07-20 : https://arxiv.org/abs/1510.00726 / 
1810.04882 / 2019-06-24 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1810_04882_towards_understand / 
1608.05426 / 2018-07-23 : https://arxiv.org/abs/1608.05426 / 
1807.06036 / 2019-04-23 : https://arxiv.org/abs/1807.06036 / 
1912.01412 / 2019-12-09 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1912_01412_deep_learning_for_ / 
1301.3781 / 2016-01-13 : http://arxiv.org/pdf/1301.3781.pdf / 
1810.04805 / 2018-10-12 : https://arxiv.org/abs/1810.04805 / 
1906.02715 / 2019-06-07 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1906_02715_visualizing_and_me / 
1503.08677 / 2020-02-18 : http://127.0.0.1:8080/semanlink/doc/2020/02/_1503_08677_label_embedding_fo / 
1905.07129 / 2019-08-05 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1905_07129_ernie_enhanced_la / 
1807.07984 / 2018-11-14 : https://arxiv.org/abs/1807.07984 / 
1809.00782 / 2018-09-06 : https://arxiv.org/abs/1809.00782 / 
1506.02142 / 2019-05-13 : https://arxiv.org/abs/1506.02142 / 
1708.00214 / 2017-08-04 : https://arxiv.org/pdf/1708.00214.pdf / 
1711.07128 / 2017-12-15 : https://arxiv.org/pdf/1711.07128.pdf / 
2002.11402 / 2020-02-27 : http://127.0.0.1:8080/semanlink/doc/2020/02/_2002_11402_detecting_potentia / 
1511.07972 / 2017-10-24 : https://arxiv.org/abs/1511.07972 / 
1706.00957 / 2017-11-11 : https://arxiv.org/pdf/1706.00957.pdf / 
1507.07998 / 2017-08-20 : https://arxiv.org/pdf/1507.07998.pdf / 
1804.04526 / 2018-04-15 : https://arxiv.org/abs/1804.04526 / 
1811.06031 / 2018-11-17 : https://arxiv.org/abs/1811.06031 / 
1908.08983 / 2019-08-28 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1908_08983_a_little_annotatio / 
1404.5367 / 2018-05-22 : https://arxiv.org/abs/1404.5367 / 
1910.03524 / 2019-10-09 : http://127.0.0.1:8080/semanlink/doc/2019/10/_1910_03524_beyond_vector_spac / 
1902.11269 / 2019-03-02 : https://arxiv.org/abs/1902.11269 / 
1802.04865 / 2018-08-27 : https://arxiv.org/abs/1802.04865 / 
1901.00596 / 2019-07-15 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1901_00596_a_comprehensive_su / 
2003.08271 / 2020-03-19 : http://127.0.0.1:8080/semanlink/doc/2020/03/_2003_08271_pre_trained_models / 
1902.10909 / 2020-01-09 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1902_10909_bert_for_joint_int / 
1812.00417 / 2019-06-28 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1812_00417_snorkel_drybell_a / 
1910.04126 / 2020-02-20 : http://127.0.0.1:8080/semanlink/doc/2020/02/_1910_04126_scalable_nearest_n / 
1707.00418 / 2018-03-16 : https://arxiv.org/abs/1707.00418 / 
1908.10084 / 2019-08-28 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1908_10084_sentence_bert_sen / 
1803.02893 / 2019-03-20 : https://arxiv.org/abs/1803.02893 / 
1512.00765 / 2017-06-09 : https://arxiv.org/abs/1512.00765 / 
1901.11504 / 2019-02-17 : https://arxiv.org/abs/1901.11504 / 
1405.4053 / 2017-07-10 : https://arxiv.org/abs/1405.4053 / 
1807.00082 / 2019-11-12 : http://127.0.0.1:8080/semanlink/doc/2019/11/_1807_00082_amanuensis_the_pr / 
2004.06842 / 2020-04-17 : http://127.0.0.1:8080/semanlink/doc/2020/04/2004_06842_layered_graph_embe / 
1909.01066 / 2019-09-05 : http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_01066_language_models_as / 
1709.02840 / 2017-09-26 : https://arxiv.org/abs/1709.02840 / 
1603.01360 / 2018-03-05 : https://arxiv.org/abs/1603.01360 / 
1902.05309 / 2019-02-18 : https://arxiv.org/abs/1902.05309v1 / 
1312.6184 / 2014-10-06 : http://arxiv.org/abs/1312.6184v5 / 
1909.03186 / 2019-09-11 : http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_03186_on_extractive_and_ / 
1801.04016 / 2018-02-21 : https://arxiv.org/abs/1801.04016 / 
1602.01137 / 2019-07-17 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1602_01137_a_dual_embedding_s / 
1911.05507 / 2020-02-11 : http://127.0.0.1:8080/semanlink/doc/2020/02/_1911_05507_compressive_transf / 
1503.02531 / 2020-04-16 : http://127.0.0.1:8080/semanlink/doc/2020/04/1503_02531_distilling_the_kno / 
2002.12327 / 2020-02-28 : http://127.0.0.1:8080/semanlink/doc/2020/02/_2002_12327_a_primer_in_bertol / 
1903.05823 / 2019-03-18 : https://arxiv.org/abs/1903.05823 / 
1705.08039 / 2017-12-16 : https://arxiv.org/pdf/1705.08039.pdf / 
2004.05150 / 2020-04-13 : http://127.0.0.1:8080/semanlink/doc/2020/04/2004_05150_longformer_the_lo / 
1902.05196 / 2019-02-18 : https://arxiv.org/abs/1902.05196v1 / 
cs.DS/031 / 2006-10-18 : http://arxiv.org/pdf/cs.DS/0310019 / 
javax.ws.rs.BadRequestException: HTTP 400 Bad Request
	at org.glassfish.jersey.client.JerseyInvocation.convertToException(JerseyInvocation.java:1077)
	at org.glassfish.jersey.client.JerseyInvocation.translate(JerseyInvocation.java:883)
	at org.glassfish.jersey.client.JerseyInvocation.lambda$invoke$1(JerseyInvocation.java:767)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:316)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:298)
	at org.glassfish.jersey.internal.Errors.process(Errors.java:229)
	at org.glassfish.jersey.process.internal.RequestScope.runInScope(RequestScope.java:414)
	at org.glassfish.jersey.client.JerseyInvocation.invoke(JerseyInvocation.java:765)
	at org.glassfish.jersey.client.JerseyInvocation$Builder.method(JerseyInvocation.java:428)
	at org.glassfish.jersey.client.JerseyInvocation$Builder.get(JerseyInvocation.java:324)
	at net.semanlink.arxiv.ArxivEntry.newArxivEntry(ArxivEntry.java:47)
	at net.semanlink.arxiv.ArxivMetadataExtractor.doIt(ArxivMetadataExtractor.java:39)
	at net.semanlink.arxiv.ArxivLoopUpdateTest.ATTENTION_MODIF_VRAI_SEMANLINK_FPS(ArxivLoopUpdateTest.java:74)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:497)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
	at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:86)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:538)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:760)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:460)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:206)
1608.04062 / 2016-09-03 : http://arxiv.org/pdf/1608.04062v1.pdf / 
1602.06797 / 2017-11-07 : https://arxiv.org/abs/1602.06797 / 
1912.12510 / 2020-01-15 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1912_12510_detecting_out_of_d / 
1503.03832 / 2020-01-25 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1503_03832_facenet_a_unified / 
1703.07464 / 2020-02-09 : http://127.0.0.1:8080/semanlink/doc/2020/02/_1703_07464_no_fuss_distance_m / 
1807.03748 / 2018-07-21 : https://arxiv.org/abs/1807.03748 / 
1805.03793 / 2018-05-22 : https://arxiv.org/abs/1805.03793 / 
2002.05867 / 2020-02-17 : http://127.0.0.1:8080/semanlink/doc/2020/02/_2002_05867v1_transformers_as_ / 
1710.06632 / 2018-10-09 : https://arxiv.org/abs/1710.06632 / 
1904.13001 / 2019-07-04 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1904_13001_encoding_categoric / 
1811.09386 / 2019-06-23 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1811_09386_explicit_interacti / 
1706.03762 / 2018-10-12 : https://arxiv.org/abs/1706.03762 / 
1903.04197 / 2020-04-16 : http://127.0.0.1:8080/semanlink/doc/2020/04/1903_04197_structured_knowled / 
1901.02860 / 2019-01-11 : https://arxiv.org/abs/1901.02860 / 
1709.03856 / 2018-05-13 : https://arxiv.org/abs/1709.03856 / 
2001.07685 / 2020-01-22 : http://127.0.0.1:8080/semanlink/doc/2020/01/_2001_07685_fixmatch_simplify / 
1912.03927 / 2019-12-11 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1912_03927_large_deviations_f / 
1806.05662 / 2018-06-23 : https://arxiv.org/abs/1806.05662 / 
0811.3701 / 2009-01-20 : http://arxiv.org/abs/0811.3701 / 
1604.00289 / 2018-10-28 : https://arxiv.org/abs/1604.00289 / 
1905.10070 / 2019-06-22 : http://127.0.0.1:8080/semanlink/doc/2019/06/_1905_10070_label_aware_docume / 
1911.01464 / 2019-11-06 : http://127.0.0.1:8080/semanlink/doc/2019/11/_1911_01464_emerging_cross_lin / 
1604.06737 / 2018-03-03 : https://arxiv.org/abs/1604.06737 / 
1704.08803 / 2019-01-27 : https://arxiv.org/abs/1704.08803 / 
1503.02406 / 2019-08-15 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1503_02406_deep_learning_and_ / 
1806.04470 / 2018-06-28 : https://arxiv.org/abs/1806.04470 / 
1806.06259 / 2018-06-19 : https://arxiv.org/abs/1806.06259 / 
1905.06088 / 2020-03-15 : http://127.0.0.1:8080/semanlink/doc/2020/03/_1905_06088_neural_symbolic_co / 
1701.00185 / 2017-11-04 : https://arxiv.org/pdf/1701.00185.pdf / 
1912.08904 / 2020-01-01 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1912_08904_macaw_an_extensib / 
1808.07699 / 2019-04-23 : https://arxiv.org/abs/1808.07699 / 
1802.01021 / 2019-04-25 : https://arxiv.org/abs/1802.01021 / 
1412.1897 / 2017-08-24 : https://arxiv.org/pdf/1412.1897v4.pdf / 
physics/0004057 / 2019-08-15 : http://127.0.0.1:8080/semanlink/doc/2019/08/_physics_0004057_the_informati / 
1602.02410 / 2016-02-09 : http://arxiv.org/abs/1602.02410 / 
1812.05944 / 2019-06-18 : http://127.0.0.1:8080/semanlink/doc/2019/06/a_tutorial_on_distance_metric_l / 
1011.4088 / 2019-10-13 : http://127.0.0.1:8080/semanlink/doc/2019/10/_1011_4088_an_introduction_to_ / 
1909.01380 / 2019-09-16 : http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_01380_the_bottom_up_evol / 
1804.01486 / 2018-04-14 : https://arxiv.org/abs/1804.01486 / 
1910.09760 / 2019-11-06 : http://127.0.0.1:8080/semanlink/doc/2019/11/_1910_09760_question_answering / 
1902.10618 / 2019-02-28 : https://arxiv.org/abs/1902.10618 / 
1704.05358 / 2018-10-06 : https://arxiv.org/abs/1704.05358 / 
1503.00759 / 2017-10-24 : https://arxiv.org/abs/1503.00759 / 
2002.02925 / 2020-02-10 : http://127.0.0.1:8080/semanlink/doc/2020/02/_2002_02925_bert_of_theseus_c / 
1503.08895 / 2018-10-23 : https://arxiv.org/abs/1503.08895 / 
1905.12149 / 2019-05-31 : http://127.0.0.1:8080/semanlink/doc/2019/05/_1905_12149_satnet_bridging_d / 
1511.06335 / 2019-02-19 : https://arxiv.org/abs/1511.06335 / 
1710.04099 / 2018-02-13 : https://arxiv.org/abs/1710.04099 / 
1607.07956 / 2018-05-12 : https://arxiv.org/abs/1607.07956 / 
2003.03384 / 2020-03-17 : http://127.0.0.1:8080/semanlink/doc/2020/03/_2003_03384_automl_zero_evolv / 
1911.00172 / 2019-12-20 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1911_00172_generalization_thr / 
1709.07604 / 2019-05-29 : http://127.0.0.1:8080/semanlink/doc/2019/05/_1709_07604_a_comprehensive_su / 
1907.07355 / 2019-07-24 : http://127.0.0.1:8080/semanlink/doc/2019/07/_1907_07355_probing_neural_net / 
1912.03263 / 2019-12-09 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1912_03263_your_classifier_is / 
1909.03193 / 2020-03-22 : http://127.0.0.1:8080/semanlink/doc/2020/03/_1909_03193_kg_bert_bert_for_ / 
1711.00046 / 2020-01-09 : http://127.0.0.1:8080/semanlink/doc/2020/01/_1711_00046_replace_or_retriev / 
1801.01586 / 2018-01-09 : https://arxiv.org/abs/1801.01586 / 
1810.00438 / 2018-10-06 : https://arxiv.org/abs/1810.00438 / 
1707.00306 / 2019-12-11 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1707_00306_variable_selection / 
1809.01797 / 2018-09-07 : https://arxiv.org/abs/1809.01797 / 
1806.01261 / 2018-06-13 : https://arxiv.org/abs/1806.01261 / 
1805.04032 / 2018-05-30 : https://arxiv.org/abs/1805.04032 / 
1909.04939 / 2019-09-28 : http://127.0.0.1:8080/semanlink/doc/2019/09/_1909_04939_inceptiontime_fin / 
1506.08422 / 2017-12-03 : https://arxiv.org/abs/1506.08422 / 
1909.02164 / 2019-12-01 : http://127.0.0.1:8080/semanlink/doc/2019/12/_1909_02164_tabfact_a_large_s / 
1810.09164 / 2019-04-26 : https://arxiv.org/abs/1810.09164 / 
1812.09449 / 2019-04-24 : https://arxiv.org/abs/1812.09449 / 
1905.07854 / 2019-08-23 : http://127.0.0.1:8080/semanlink/doc/2019/08/_1905_07854_kgat_knowledge_gr / 
1412.6623 / 2018-01-28 : https://arxiv.org/abs/1412.6623 / 
2002.04688 / 2020-02-13 : http://127.0.0.1:8080/semanlink/doc/2020/02/_2002_04688_fastai_a_layered_ / 
1602.05314 / 2016-02-26 : http://arxiv.org/abs/1602.05314 / 
1611.04228 / 2017-04-28 : https://arxiv.org/abs/1611.04228 / 
0807.4145 / 2008-08-17 : http://arxiv.org/abs/0807.4145 / 
1609.08496 / 2017-06-07 : https://arxiv.org/abs/1609.08496 / 
1607.01759 / 2017-09-10 : https://arxiv.org/abs/1607.01759 / 
1703.03129 / 2018-10-23 : https://arxiv.org/abs/1703.03129 / 
1904.01947 / 2020-04-02 : http://127.0.0.1:8080/semanlink/doc/2020/04/1904_01947_extracting_tables_ / 
1002.2284 / 2013-05-11 : http://arxiv.org/abs/1002.2284v2 / 
1601.03764 / 2018-08-28 : https://arxiv.org/abs/1601.03764 / 
2003.00330 / 2020-03-15 : http://127.0.0.1:8080/semanlink/doc/2020/03/_2003_00330_graph_neural_netwo / 
1601.00670 / 2018-08-07 : https://arxiv.org/abs/1601.00670 / 
1802.07044 / 2019-10-11 : http://127.0.0.1:8080/semanlink/doc/2019/10/_1802_07044_the_description_le / 
1903.05872 / 2019-03-17 : https://arxiv.org/abs/1903.05872v1 / 
1103.0398 / 2018-01-17 : https://arxiv.org/abs/1103.0398 / 
2001.01447 / 2020-01-09 : http://127.0.0.1:8080/semanlink/doc/2020/01/_2001_01447v1_improving_entity / 
1712.01208 / 2017-12-11 : https://arxiv.org/abs/1712.01208v1 / 
1803.01271 / 2018-08-05 : https://arxiv.org/abs/1803.01271 / 
1605.07427 / 2018-11-14 : https://arxiv.org/abs/1605.07427 / 
1710.04087 / 2017-10-14 : https://arxiv.org/abs/1710.04087 / 
1511.08855 / 2017-11-19 : https://arxiv.org/abs/1511.08855 / 
1801.00631 / 2018-01-03 : https://arxiv.org/abs/1801.00631 / 
*/
