package cn.com.xiaofabo.scia.aiawardcheck.fileprocessor;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.xiaofabo.scia.aiawardcheck.util.CommonUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.com.xiaofabo.scia.aiawardcheck.entity.Award;
import cn.com.xiaofabo.scia.aiawardcheck.entity.Pair;
import cn.com.xiaofabo.scia.aiawardcheck.entity.Party;
import cn.com.xiaofabo.scia.aiawardcheck.entity.Routine;
import org.springframework.util.StringUtils;

public class AwardReader extends DocReader {
	public static Logger logger = Logger.getLogger(AwardReader.class.getName());

	/// Regular expressions for reading award document
	private static final String REGEX_ROUTINE_DATE = "\\b.*年.*月.*日";
	private static final String REGEX_ROUTINE_LOCATION = "深圳";
	private static final String REGEX_ROUTINE_ID = ".*深国仲裁.*号";
	private static final String REGEX_ROUTINE_PROPOSER = "^(第.)?申\\s*请\\s*人";
	private static final String REGEX_ROUTINE_RESPONDER = "^(第.)?被\\s*申\\s*请\\s*人";
	private static final String REGEX_ROUTINE_PARTY = "^(第.)?被?\\s*申\\s*请\\s*人";
	private static final String REGEX_ROUTINE_END = "^深\\s*圳$";

	private static final String REGEX_JIANJIE_START = "深圳国际仲裁院（";

	private static final String REGEX_CASE_TITLE = "一、案情";
	private static final String REGEX_CASE_PROPOSER_TEXT_TITLE = "）申请人的仲裁请求、事实及理由";
	private static final String REGEX_CASE_REPLY_TEXT_TITLE = ".*被申请人的主要答辩意见";
	private static final String REGEX_CASE_PROPOSER_EVIDENCE_TEXT_TITLE = "）申请人提交的证据";
	private static final String REGEX_CASE_RESPONDER_EVIDENCE_TEXT_TITLE = "）被申请人提交的证据";
	private static final String REGEX_CASE_COUNTER_CLAIM_TEXT_TITLE = "）被申请人的仲裁反请求、事实及理由";
	private static final String REGEX_CASE_COUNTER_COUNTER_CLAIM_TEXT_TITLE = "）申请人就反请求的主要答辩意见";
	private static final String REGEX_CASE_PROPOSER_AGENT_TEXT_TITLE = "）申请人(代理人)?的主要(代理)?意见";
	private static final String REGEX_CASE_RESPONDENT_AGENT_TEXT_TITLE = "）被申请人(代理人)?的主要(代理)?意见";

	private static final String REGEX_ARBIOP_TITLE = "仲裁庭意见";
	private static final String REGEX_ARBIOP_FACT_TEXT_TITLE = "）仲裁庭.*认定的事实";
	private static final String REGEX_ARBIOP_FOREIGN_CASE_TEXT_TITLE = "）关于本案法律适用问题";
	private static final String REGEX_ARBIOP_CONTRACT_REGULATION_TEXT_TITLE = "）关于本案合同的效力";
	private static final String REGEX_ARBIOP_FOCUS_TEXT_TITLE = "）关于本案的争议焦点";
	private static final String REGEX_ARBIOP_REQUEST_TEXT_TITLE = "）关于申请人的仲裁请求";
	private static final String REGEX_ARBIOP_COUNTER_REQUEST_TEXT_TITLE = "）关于被申请人的仲裁反请求";
	private static final String REGEX_ARBIOP_RESPONDENT_ABSENT_TEXT_TITLE = "）被申请人缺席的法律后果";
	private static final String CAIJUE_START = "仲裁庭对本案作出裁决如下";
	private static final String END = "仲裁员：";

	private final List<Party> partyList;

	public AwardReader() {
		super();
		partyList = new LinkedList<Party>();
	}

	public Award buildAward(String inputPath) throws IOException {
		readWordFile(inputPath);
		return buildAward();
	}

	public Award buildAward(File inputFile) throws IOException {
		readWordFile(inputFile);
		return buildAward();
	}

	private Award buildAward() throws IOException {
		String lines[] = docText.split("\\r?\\n");
		readParties(lines);

		String dateText = "";
		String caseIdText = "";

		List jianjieText = new LinkedList<String>();

		int routineTextLineStart = 0;
		int routineTextLineEnd = 0;
		List routineText = new LinkedList<String>();

		int caseTextLineStart = 0;
		int caseTextLineEnd = 0;
		List caseText = new LinkedList<String>();

		int arbiOpinionTextLineStart = 0;
		int arbiOpinionTextLineEnd = 0;
		List arbiOpinionText = new LinkedList<String>();

		int arbitramentTextLineStart = 0;
		int arbitramentTextLineEnd = 0;
		List arbitramentText = new LinkedList<String>();

		List endTable = new LinkedList<String>();

		lines = docText.split("\\r?\\n");
		int startLineNum = 0, endLineNum = 0;
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i].trim();
			if (removeAllSpaces(line).equals(REGEX_ROUTINE_LOCATION)) {
				int lineIndex = i;
				while (lineIndex < lines.length) {
					String l = lines[lineIndex++];
					if (removeAllSpaces(l).matches(REGEX_ROUTINE_DATE)) {
						dateText = l;
						break;
					}
				}
			}

			if (line.matches(REGEX_ROUTINE_ID)) {
				caseIdText = line;
				routineTextLineStart = i + 1;
			}

			if (removeAllSpaces(line).contains(REGEX_JIANJIE_START)) {
				int lineIndex = i;
				while (true) {
					String l = lines[lineIndex++];
					jianjieText.add(l);
					if (StringUtils.isEmpty(CommonUtil.replaceBlank(l))) {
						break;
					}
				}
			}


			if (removeAllSpaces(line).contains(REGEX_CASE_TITLE)) {
				routineTextLineEnd = i - 1;
				caseTextLineStart = i + 1;

				for (int index = routineTextLineStart; index < routineTextLineEnd + 1; ++index) {
					if (!lines[index].isEmpty()) {
						routineText.add(lines[index]);
					}
				}
			}

			if (removeAllSpaces(line).contains(REGEX_ARBIOP_TITLE)) {
				caseTextLineEnd = i - 1;
				arbiOpinionTextLineStart = i + 1;

				for (int index = caseTextLineStart; index < caseTextLineEnd + 1; ++index) {
					if (!lines[index].isEmpty()) {
						caseText.add(lines[index]);
					}
				}
			}

			if (removeAllSpaces(line).contains("三、裁决")) {
				arbiOpinionTextLineEnd = i - 1;
				arbitramentTextLineStart = i + 1;

				for (int index = arbiOpinionTextLineStart; index < arbiOpinionTextLineEnd + 1; ++index) {
					if (!lines[index].isEmpty()) {
						arbiOpinionText.add(lines[index]);
					}
				}
			}

			if (removeAllSpaces(line).contains(END)) {
				arbitramentTextLineEnd = i - 1;

				for (int index = arbitramentTextLineStart; index < arbitramentTextLineEnd + 1; ++index) {
					if (!lines[index].isEmpty()) {
						arbitramentText.add(lines[index]);
					}
				}
			}

			if (removeAllSpaces(line).contains(END)) {
				arbitramentTextLineEnd = i - 1;

				for (int index = arbitramentTextLineEnd; index < lines.length ; ++index) {
					if (!lines[index].isEmpty()) {
						endTable.add(lines[index]);
					}
				}
			}
		}

		/*if (arbitramentTextLineEnd == 0) {
			arbitramentTextLineEnd = lines.length - 1;
			for (int index = arbitramentTextLineStart; index < arbitramentTextLineEnd + 1; ++index) {
				if (!StringUtils.isEmpty(CommonUtil.replaceBlank(lines[index]))) {
					arbitramentText.add(lines[index]);
				}
			}
		}*/

		Award award = new Award();
		award.setDateText(dateText);
		award.setCaseIdText(caseIdText);
		award.setPartyList(partyList);
		award.setJianjieText(jianjieText);
		award.setRoutineText(routineText);

		/// 一、案情
		award = divideCaseText(award, caseText);
		/// 二、仲裁庭意见
		award = divideArbiOpinionText(award, arbiOpinionText);
		/// 三、裁 决
		dealArbitramentText(arbitramentText);
		award.setArbitramentText(arbitramentText);
		dealEndTable(endTable);
		award.setFootText(endTable);

		return award;
	}

	private static void dealArbitramentText(List<String> list){
		for (int i = 0; i < list.size(); i ++){
			if (list.get(i).contains(CAIJUE_START)){
				continue;
			}
			if (list.get(i).startsWith("    （") || list.get(i).startsWith("    (")
					|| list.get(i).replaceAll("\\s*","").startsWith("（")
					|| list.get(i).replaceAll("\\s*","").startsWith("(")){
				return;
			}
			list.set(i, "    （" + DocUtil.numberToCN(i)+"）"+list.get(i));
		}
	}

	private static void dealEndTable(List<String> list){
		for (Iterator<String> ite = list.iterator(); ite.hasNext();) {
			String str = ite.next();
			try{
				Integer.parseInt(str);
				ite.remove();
			} catch (Exception e){
				// do nothing
			}
		}
	}

	private void readParties(String[] lines) {
		List<Integer> partyChunckStartIdx = new LinkedList();
		int lastIdx = 0;
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i].trim();
			Pattern pattern = Pattern.compile(REGEX_ROUTINE_PARTY);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				partyChunckStartIdx.add(i);
			}
			pattern = Pattern.compile(REGEX_ROUTINE_END);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				lastIdx = i;
				break;
			}
		}

		for (int i = 0; i < partyChunckStartIdx.size(); ++i) {
			int startIdx = partyChunckStartIdx.get(i);
			int endIdx = (i == partyChunckStartIdx.size() - 1) ? lastIdx : partyChunckStartIdx.get(i + 1) - 1;
			Party party = new Party();
			int currentPairIndex = -1;
			for (int idx = startIdx; idx < endIdx; ++idx) {
				String line = lines[idx].trim();
				if (!line.contains("：") && currentPairIndex != -1) {
					String value = line;
					Pair pair = party.getProperty(currentPairIndex);
					if (!StringUtils.isEmpty(CommonUtil.replaceBlank(value))){
						pair.setValue(party.getProperty(currentPairIndex).getValue() + "\n\n" + value);
					}
				} else {
					String key = line.substring(0, line.indexOf("："));
					String value = line.substring(line.indexOf("：") + 1);
					party.addProperty(new Pair(key, value));
					++currentPairIndex;
				}
			}
			partyList.add(party);
		}
	}

	private Award divideCaseText(Award award, List caseText) {
		/// Further divide case text

		/// proposer, reply, counterClaim,
		/// proposerEvidence, respondentEvidence
		/// counterCounterClaim, proposerAgent,
		/// respondentAgent
		int[] startPos = { 0, 0, 0, 0, 0, 0, 0, 0 };

		/// proposer, reply, counterClaim,
		/// proposerEvidence, respondentEvidence
		/// counterCounterClaim, proposerAgent,
		/// respondentAgent
		int[] endPos = { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < caseText.size(); ++i) {
			String line = (String) caseText.get(i);

			Pattern pattern = Pattern.compile(REGEX_CASE_PROPOSER_TEXT_TITLE);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				startPos[0] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_REPLY_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasReply(true);
				startPos[1] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_COUNTER_CLAIM_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasCounterClaim(true);
				startPos[2] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_PROPOSER_EVIDENCE_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasProposerEvidence(true);
				startPos[3] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_RESPONDER_EVIDENCE_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasRespondentEvidence(true);
				startPos[4] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_COUNTER_COUNTER_CLAIM_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasCounterCounterClaim(true);
				startPos[5] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_PROPOSER_AGENT_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasProposerAgentClaim(true);
				startPos[6] = i + 1;
			}

			pattern = Pattern.compile(REGEX_CASE_RESPONDENT_AGENT_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasRespondentAgentClaim(true);
				startPos[7] = i + 1;
			}
		}

		for (int i = 0; i < startPos.length - 1; ++i) {
			if (startPos[i] == 0) {
				continue;
			}
			int startPosIdx = i + 1;
			while (startPosIdx < startPos.length) {
				if (startPos[startPosIdx] == 0) {
					++startPosIdx;
					continue;
				}
				endPos[i] = startPos[startPosIdx++] - 1;
				break;
			}
			if (endPos[i] == 0) {
				endPos[i] = caseText.size();
			}
		}

		endPos[startPos.length - 1] = startPos[startPos.length - 1] == 0 ? 0 : caseText.size();

		/// 案情部分
		award.setProposerText(caseText.subList(startPos[0], endPos[0]));
		award.setReplyText(caseText.subList(startPos[1], endPos[1]));
		award.setCounterClaimText(caseText.subList(startPos[2], endPos[2]));
		award.setProposerEvidenceText(caseText.subList(startPos[3], endPos[3]));
		award.setResponderEvidenceText(caseText.subList(startPos[4], endPos[4]));
		award.setCounterCounterClaimText(caseText.subList(startPos[5], endPos[5]));
		award.setProposerAgentClaimText(caseText.subList(startPos[6], endPos[6]));
		award.setRespondentAgentClaimText(caseText.subList(startPos[7], endPos[7]));
		return award;
	}

	private Award divideArbiOpinionText(Award award, List arbiOpinionText) {
		/// Further divide arbitration opinion text

		int[] startPos = { 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] endPos = { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < arbiOpinionText.size(); ++i) {
			String line = (String) arbiOpinionText.get(i);

			startPos[0] = 0;

			Pattern pattern = Pattern.compile(REGEX_ARBIOP_FACT_TEXT_TITLE);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				startPos[1] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_FOREIGN_CASE_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setForeignCase(true);
				startPos[2] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_CONTRACT_REGULATION_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasContractRegulation(true);
				startPos[3] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_FOCUS_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasFocus(true);
				startPos[4] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_REQUEST_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasRequest(true);
				startPos[5] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_COUNTER_REQUEST_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setHasCounterRequest(true);
				startPos[6] = i + 1;
			}

			pattern = Pattern.compile(REGEX_ARBIOP_RESPONDENT_ABSENT_TEXT_TITLE);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				award.setRespondentAbsent(true);
				startPos[7] = i + 1;
			}
		}

		for (int i = 0; i < startPos.length - 1; ++i) {
			if (i != 0 && startPos[i] == 0) {
				continue;
			}
			int startPosIdx = i + 1;
			while (startPosIdx < startPos.length) {
				if (startPos[startPosIdx] == 0) {
					++startPosIdx;
					continue;
				}
				endPos[i] = startPos[startPosIdx++] - 1;
				break;
			}
			if (endPos[i] == 0) {
				endPos[i] = arbiOpinionText.size();
			}
		}

		endPos[startPos.length - 1] = startPos[startPos.length - 1] == 0 ? 0 : arbiOpinionText.size();

		award.setArbiPreStatementText(arbiOpinionText.subList(startPos[0], endPos[0]));
		award.setArbiOpFactText(arbiOpinionText.subList(startPos[1], endPos[1]));
		award.setForeignCaseLawText(arbiOpinionText.subList(startPos[2], endPos[2]));
		award.setContractRegulationText(arbiOpinionText.subList(startPos[3], endPos[3]));
		award.setFocusText(arbiOpinionText.subList(startPos[4], endPos[4]));
		award.setRequestText(arbiOpinionText.subList(startPos[5], endPos[5]));
		award.setCounterRequestText(arbiOpinionText.subList(startPos[6], endPos[6]));

		return award;
	}
}
