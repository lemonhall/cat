package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;

public class WeeklyCapacityUpdater implements CapacityUpdater {

	@Inject
	private WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	public static final String ID = "weekly_capacity_updater";

	private OverloadReport generateOverloadReport(WeeklyReport report, Overload overload) {
		OverloadReport overloadReport = new OverloadReport();

		overloadReport.setDomain(report.getDomain());
		overloadReport.setIp(report.getIp());
		overloadReport.setName(report.getName());
		overloadReport.setPeriod(report.getPeriod());
		overloadReport.setReportType(CapacityUpdater.WEEKLY_TYPE);
		overloadReport.setType(report.getType());
		overloadReport.setReportLength(overload.getReportSize());

		return overloadReport;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadDao.findMaxIdByType(CapacityUpdater.WEEKLY_TYPE, OverloadEntity.READSET_MAXID).getMaxId();
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<WeeklyReportContent> weeklyReports = m_weeklyReportContentDao.findOverloadReport(loopStartId, capacity,
			      WeeklyReportContentEntity.READSET_LENGTH);

			for (WeeklyReportContent content : weeklyReports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();
					Overload overloadTable = m_overloadDao.createLocal();

					overloadTable.setReportId(reportId);
					overloadTable.setReportSize(contentLength);
					overloadTable.setReportType(CapacityUpdater.WEEKLY_TYPE);

					m_overloadDao.insert(overloadTable);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = weeklyReports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = weeklyReports.get(size - 1).getReportId();
			}
		}

		return maxId;
	}

	@Override
	public void updateOverloadReport(int updateStartId, List<OverloadReport> overloadReports) throws DalException {
		boolean hasMore = true;

		while (hasMore) {
			List<Overload> overloads = m_overloadDao.findIdAndSizeByTypeAndBeginId(CapacityUpdater.WEEKLY_TYPE,
			      updateStartId, OverloadEntity.READSET_BIGGER_ID_SIZE);

			for (Overload overload : overloads) {
				try {
					int reportId = overload.getReportId();
					WeeklyReport report = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);

					overloadReports.add(generateOverloadReport(report, overload));
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}
			if (overloads.size() < 1000) {
				hasMore = false;
			}
		}
	}

}
