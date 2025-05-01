import { APIRequestContext } from '@playwright/test'
import { TimeseriesPoint } from './types'

const API_BASE_URL = 'http://localhost:8080/api'

const parseResponse = async (response: any): Promise<any> => {
	try {
		const parsed = await response.json()
		if (parsed.ok) return parsed.body
		else return parsed
	} catch (error) {
		console.error('Error parsing response:', error)
		return response
	}
}

// Utility to create a well
export const createWell = async (request: APIRequestContext, well: any): Promise<any> => {
	const response = await request.post(`${API_BASE_URL}/wells`, { data: well })
	return await parseResponse(response)
}

// Utility to get a well by ID
export const getWellById = async (request: APIRequestContext, wellId: string): Promise<any> => {
	const response = await request.get(`${API_BASE_URL}/wells/${wellId}`)
	return await parseResponse(response)
}

// Utility to get all wells
export const getAllWells = async (request: APIRequestContext): Promise<any> => {
	const response = await request.get(`${API_BASE_URL}/wells`)
	return await parseResponse(response)
}

// Utility to delete a well
export const deleteWell = async (request: APIRequestContext, wellId: string): Promise<any> => {
	const response = await request.delete(`${API_BASE_URL}/wells/${wellId}`)
	return await parseResponse(response)
}

// Utility to insert timeseries data
export const insertTimeseries = async (
	request: APIRequestContext,
	wellId: string,
	timeseriesData: TimeseriesPoint
): Promise<any> => {
	const response = await request.post(`${API_BASE_URL}/wells/${wellId}/timeseries`, { data: timeseriesData })
	return response.json()
}

// Utility to insert timeseries data batch
export const insertTimeseriesBatch = async (
	request: APIRequestContext,
	wellId: string,
	timeseriesData: TimeseriesPoint[]
): Promise<any> => {
	const response = await request.post(`${API_BASE_URL}/wells/${wellId}/timeseries/batch`, { data: timeseriesData })
	return response.json()
}

// Utility to get timeseries data
export const getTimeseries = async (
	request: APIRequestContext,
	wellId: string,
	from: number,
	to: number
): Promise<any> => {
	const response = await request.get(`${API_BASE_URL}/wells/${wellId}/timeseries?from=${from}&to=${to}`)
	return response.json()
}

// Utility to delete timeseries data
export const deleteTimeseries = async (
	request: APIRequestContext,
	wellId: string,
	from: number,
	to: number
): Promise<any> => {
	await request.delete(`${API_BASE_URL}/wells/${wellId}/timeseries?from=${from}&to=${to}`)
}

// Utility to get well boundaries
export const getWellBoundaries = async (request: APIRequestContext, wellId: string): Promise<any> => {
	const response = await request.get(`${API_BASE_URL}/wells/${wellId}/boundaries`)
	return response.json()
}
