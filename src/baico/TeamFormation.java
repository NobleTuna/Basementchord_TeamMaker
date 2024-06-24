package baico;

import java.io.*;
import java.util.*;

public class TeamFormation {
	static String[] names; // 인원들의 이름 목록
	static int[] levels; // 인원들의 레벨 정보
	static int[][] previousTeams; // 이전 팀 정보
	static int[][] couples; // 커플 정보
	static int[] admins; // 운영진 정보
	static int[] liveLevelSum; // dfs에서 각 팀의 점수합을 들고다닐 배열
	static int teamCount; // 팀의 갯수
	static int maxMember; // 각 팀의 최대 인원 수
	static int minMember; // 각 팀의 최소 인원 수
	static List<String[][]> results = new ArrayList<>(); // 결과를 저장할 리스트
	static Map<String, Boolean> memo = new HashMap<>(); // 메모이제이션을 위한 맵
	static int avgLevel; // 평균레벨
	static int range = 1; // 가능 오차

	public static void main(String[] args) {

		try {
			// 파일에서 데이터를 읽어오는 부분
			names = readNames("naming.txt");
			levels = readLevels("guitar_level.txt");
			previousTeams = readMatrix("last_team.txt");
			couples = readMatrix("couple.txt");
			admins = readAdmins("admin.txt");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		shuffleData();

		teamCount = 0;
		for (int i = 0; i < names.length; i++) {
			if (admins[i] == 1)
				teamCount++;
		}

		minMember = names.length / teamCount;
		maxMember = minMember + 1;

		List<List<Integer>> teams = new ArrayList<>();
		for (int i = 0; i < teamCount; i++) {
			teams.add(new ArrayList<>());
		}

		boolean[] used = new boolean[names.length];
		liveLevelSum = new int[teamCount];

		// 운영진을 미리 각 팀에 배정
		int adminIdx = 0;
		for (int i = 0; i < teamCount; i++) {
			while (adminIdx < admins.length && admins[adminIdx] != 1) {
				adminIdx++;
			}
			if (adminIdx < admins.length) {
				teams.get(i).add(adminIdx);
				liveLevelSum[i] += levels[adminIdx];
				used[adminIdx] = true;
				adminIdx++;
			}
		}
		int levelSum = 0;
		for (int i = 0; i < levels.length; i++) {
			levelSum += levels[i];
		}
		avgLevel = levelSum / teamCount;
		// DFS를 통해 팀을 구성
		dfs(0, teams, used, liveLevelSum);

		try {
			// 결과를 파일로 저장
			saveResults("results.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 이름 목록을 파일에서 읽어오는 메소드
	static String[] readNames(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<String> nameList = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			nameList.add(line);
		}
		br.close();
		return nameList.toArray(new String[0]);
	}

	// 레벨 정보를 파일에서 읽어오는 메소드
	static int[] readLevels(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<Integer> levelList = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			levelList.add(Integer.parseInt(line));
		}
		br.close();
		return levelList.stream().mapToInt(i -> i).toArray();
	}

	// 행렬 데이터를 파일에서 읽어오는 메소드 (이전 팀 정보와 커플 정보에 사용)
	static int[][] readMatrix(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<int[]> matrixList = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");
			int[] row = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();
			matrixList.add(row);
		}
		br.close();
		return matrixList.toArray(new int[0][]);
	}

	// 운영진 정보를 파일에서 읽어오는 메소드
	static int[] readAdmins(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		List<Integer> adminList = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			adminList.add(Integer.parseInt(line));
		}
		br.close();
		return adminList.stream().mapToInt(i -> i).toArray();
	}

	// 데이터 섞기 메소드
	static void shuffleData() {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);

		String[] shuffledNames = new String[names.length];
		int[] shuffledLevels = new int[levels.length];
		int[][] shuffledPreviousTeams = new int[names.length][names.length];
		int[][] shuffledCouples = new int[names.length][names.length];
		int[] shuffledAdmins = new int[admins.length];

		for (int i = 0; i < names.length; i++) {
			shuffledNames[i] = names[indices.get(i)];
			shuffledLevels[i] = levels[indices.get(i)];
			shuffledAdmins[i] = admins[indices.get(i)];
			for (int j = 0; j < names.length; j++) {
				shuffledPreviousTeams[i][j] = previousTeams[indices.get(i)][indices.get(j)];
				shuffledCouples[i][j] = couples[indices.get(i)][indices.get(j)];
			}
		}

		names = shuffledNames;
		levels = shuffledLevels;
		previousTeams = shuffledPreviousTeams;
		couples = shuffledCouples;
		admins = shuffledAdmins;
	}

	// DFS를 통해 팀을 구성하는 메소드
	static void dfs(int idx, List<List<Integer>> teams, boolean[] used, int[] liveLevelSum) {
		if (results.size() >= 1) {
			return; // 결과가 1개 이상이면 더 이상 탐색하지 않음
		}

		if (idx == names.length) {
			if (isValid(teams)) {
				String[][] result = new String[teamCount][];
				for (int i = 0; i < teamCount; i++) {
					result[i] = new String[teams.get(i).size()];
					for (int j = 0; j < teams.get(i).size(); j++) {
						result[i][j] = names[teams.get(i).get(j)];
					}
				}
				results.add(result);
			}
			return;
		}

		if (used[idx]) {
			dfs(idx + 1, teams, used, liveLevelSum);
			return;
		}

//		String state = getState(teams);
//		if (memo.containsKey(state)) {
//			return;
//		}

		for (int i = 0; i < teamCount; i++) {

			boolean isDup = false;

			for (int member : teams.get(i)) {
				if ((previousTeams[idx][member] == 1 || previousTeams[member][idx] == 1)
						&& (couples[idx][member] != 1 || couples[member][idx] != 1)) {
					isDup = true;
					break;
				}
			}
			if (isDup)
				continue;

			if (teams.get(i).size() == maxMember)
				continue;

			if (liveLevelSum[i] + levels[idx] > avgLevel + range)
				continue;

			if (teams.get(i).size() + 1 == maxMember && liveLevelSum[i] + levels[idx] < avgLevel - range)
				continue;

			teams.get(i).add(idx);
			used[idx] = true;
			liveLevelSum[i] += levels[idx];
			dfs(idx + 1, teams, used, liveLevelSum);
			used[idx] = false;
			teams.get(i).remove(teams.get(i).size() - 1);
			liveLevelSum[i] -= levels[idx];

		}
//		memo.put(state, true);
	}

	// 구성된 팀이 유효한지 검사하는 메소드
	static boolean isValid(List<List<Integer>> teams) {
		for (int i = 0; i < teamCount; i++) {
			if (teams.get(i).size() < minMember)
				return false;
		}

		int[] levelSums = new int[teamCount];
		boolean[] hasAdmin = new boolean[teamCount];

		for (int i = 0; i < teamCount; i++) {
			for (int member : teams.get(i)) {
				levelSums[i] += levels[member];
				if (admins[member] == 1) {
					hasAdmin[i] = true;
				}
				for (int couple = 0; couple < names.length; couple++) {
					if (couples[member][couple] == 1 && !teams.get(i).contains(couple)) {
						return false;
					}
				}
			}
		}
		printTeam(teams, liveLevelSum);
		return true;
	}

	// 현재 팀 상태를 문자열로 변환하여 해시 맵에 사용
	static String getState(List<List<Integer>> teams) {
		StringBuilder sb = new StringBuilder();
		for (List<Integer> team : teams) {
			sb.append(team.toString()).append("|");
		}
		return sb.toString();
	}

	// 결과를 파일로 저장하는 메소드
	static void saveResults(String filename) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		for (int i = 0; i < Math.min(results.size(), 5); i++) {
			for (String[] team : results.get(i)) {
				bw.write(Arrays.toString(team));
				bw.newLine();
			}
			bw.newLine();
		}
		bw.close();
	}

	static void printTeam(List<List<Integer>> teams, int[] liveLevelSum) {
		////
		for (int i = 0; i < teamCount; i++) {
			for (int j = 0; j < teams.get(i).size(); j++) {
				System.out.print(names[teams.get(i).get(j)] + " ");
			}
			System.out.println(": " + liveLevelSum[i]);

		}
		System.out.println();
		////
	}
}
