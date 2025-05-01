import { test, expect, APIRequestContext } from '@playwright/test'
import { createWell, getWellById, deleteWell } from '../../utils/apiUtils'
import { wells } from './data'

test.describe('Delete Well API Tests', () => {
	let request: APIRequestContext
	let wellId: string

	test.beforeAll(async ({ playwright }) => {
		request = await playwright.request.newContext()

		// Create a well
		const well = wells[0]
		const createdWell = await createWell(request, well)
		wellId = createdWell.id
	})

	test.afterAll(async () => {
		await request.dispose()
	})

	test('should delete a well and verify it no longer exists', async () => {
		// Delete the well
		await deleteWell(request, wellId)

		// Verify the well no longer exists
		const response = await getWellById(request, wellId)
		expect(response.ok).toBe(false)
	})
})
